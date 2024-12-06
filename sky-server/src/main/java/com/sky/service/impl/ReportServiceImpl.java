package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
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
}
