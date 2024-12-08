package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
