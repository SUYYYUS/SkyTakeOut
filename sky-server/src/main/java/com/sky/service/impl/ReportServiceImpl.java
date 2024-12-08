package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间区域的每天收入
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistic(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        //先计算日期
        List<LocalDate> dateTimeList = getDateList(begin, end);
        String join = StringUtil.join(dateTimeList, ",");//拼接成字符串
        turnoverReportVO.setDateList(join);

        List<Double> res = new ArrayList<>();
        //再计算营业额
        for (LocalDate time : dateTimeList) {
            //营业额状态是订单列表中状态为已完成的订单
            //当天的开始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(time, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(time, LocalTime.MAX);

            //map集合存放信息
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            //查询当天总营业额
            Double i =  orderMapper.sumByMap(map);
            i = i == null ? 0.0 : i;
            //添加
            res.add(i);
        }
        String sumList = StringUtil.join(res, ",");
        turnoverReportVO.setTurnoverList(sumList);
        return turnoverReportVO;
    }

    /**
     * 统计用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        //先计算日期
        List<LocalDate> dateTimeList = getDateList(begin, end);
        String dateList = StringUtil.join(dateTimeList, ",");//拼接成字符串

        //统计用户总量
        List<Integer> userTotalList = new ArrayList<>();
        //统计新增用户数量
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate time : dateTimeList) {
            //当天的开始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(time, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(time, LocalTime.MAX);
            Map map = new HashMap();
            //查询总用户数量
            map.put("begin", beginTime);
            int userCount = userMapper.getUserCount(map);
            userTotalList.add(userCount);
            //查询新增用户
            map.put("end", endTime);
            int newUserCount = userMapper.getUserCount(map);
            newUserList.add(newUserCount);
        }
        //转换为字符串
        String userCountList = StringUtils.join(userTotalList, ",");
        String newUserCountList = StringUtils.join(newUserList, ",");
        //封装对象
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(dateList);
        userReportVO.setTotalUserList(userCountList);
        userReportVO.setNewUserList(newUserCountList);
        //返回结果
        return userReportVO;
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //先计算日期
        List<LocalDate> dateTimeList = getDateList(begin, end);
        String dateList = StringUtil.join(dateTimeList, ",");//拼接成字符串

        List<Integer> ordersCount = new ArrayList<>(); //总订单每日数量
        List<Integer> ordersCountPositive = new ArrayList<>(); //有效订单每日数量

        for (LocalDate time : dateTimeList) {
            //当天的开始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(time, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(time, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            //先查询当天总订单数
            int ordersCount1 = orderMapper.getOrdersCount(map);
            ordersCount.add(ordersCount1);
            //再查询有效订单数
            map.put("status", Orders.COMPLETED);
            int ordersCount2 = orderMapper.getOrdersCount(map);
            ordersCountPositive.add(ordersCount2);
        }
        //集合转为字符串
        String join = StringUtils.join(ordersCount, ",");
        String join1 = StringUtils.join(ordersCountPositive, ",");

        int sum1, sum2 = 0;
        sum1 = ordersCount.stream().mapToInt(oc -> oc).sum();
        sum2 = ordersCountPositive.stream().mapToInt(oc -> oc).sum();
        Double orderCompletionRate = 0.0;
        if(sum1 != 0){
            orderCompletionRate = ( (double) sum2 / (double) sum1 );
        }


        //封装对象
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .orderCountList(join)
                .validOrderCountList(join1)
                .totalOrderCount(sum1)
                .validOrderCount(sum2)
                .dateList(dateList)
                .orderCompletionRate(orderCompletionRate)
                .build();

        //返回对象
        return orderReportVO;
    }

    /**
     * 获取销量前10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> nameAndCount = orderMapper.getNameAndCount(beginTime, endTime);
        if(nameAndCount.size() == 0){
            return null;
        }

        //取出每一个菜品名字
        List<String> nameList = nameAndCount.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        //取出每一个菜品的数量
        List<Integer> countList = nameAndCount.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        //转化字符串
        String nameList2String = StringUtils.join(nameList, ",");
        String countList2String = StringUtils.join(countList, ",");

        //封装对象返回
        return SalesTop10ReportVO.builder()
                .numberList(countList2String)
                .nameList(nameList2String)
                .build();
    }

    /**
     * 导出数据报表
     * @param response
     */
    @Override
    public void exportBussinessData(HttpServletResponse response) {
        //查询数据库获取营业数据，最近30天的
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTime.of(LocalDate.from(begin), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(LocalDate.from(end), LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);


        //写入文件中
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //填充数据
            XSSFSheet sheet1 = excel.getSheet("Sheet1");
            XSSFRow row = sheet1.getRow(1);
            row.getCell(1).setCellValue("时间：" + beginTime + "到" + endTime);
            XSSFRow row4 = sheet1.getRow(3);
            row4.getCell(2).setCellValue(businessData.getTurnover());
            row4.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row4.getCell(6).setCellValue(businessData.getNewUsers());
            XSSFRow row5 = sheet1.getRow(4);
            row5.getCell(2).setCellValue(businessData.getValidOrderCount());
            row5.getCell(4).setCellValue(businessData.getUnitPrice());

            //填充明细数据，30天
            for (int i = 0; i < 30; i++){
                LocalDate date = begin.plusDays(i); //一直向上加一天
                //查询某一天的营业数据
                BusinessDataVO businessDataVO = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                //从第八行开始填充
                XSSFRow row1 = sheet1.getRow(i + 7);
                row1.getCell(1).setCellValue(String.valueOf(date));
                row1.getCell(2).setCellValue(businessDataVO.getTurnover());
                row1.getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                row1.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                row1.getCell(5).setCellValue(businessDataVO.getUnitPrice());
                row1.getCell(6).setCellValue(businessDataVO.getNewUsers());
            }

            //通过输出流下载到客户端浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            //关闭资源
            outputStream.close();
            excel.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取时间段的集合
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end){
        List<LocalDate> dateTimeList = new ArrayList<>(); //用于存放日期范围内的每天日期
        dateTimeList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateTimeList.add(begin);
        }
        return dateTimeList;
    }


}
