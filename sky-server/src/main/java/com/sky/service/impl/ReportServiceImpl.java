package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<LocalDate> dateTimeList = new ArrayList<>(); //用于存放日期范围内的每天日期
        dateTimeList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateTimeList.add(begin);
        }
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
        List<LocalDate> dateTimeList = new ArrayList<>(); //用于存放日期范围内的每天日期
        dateTimeList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateTimeList.add(begin);
        }
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
}
