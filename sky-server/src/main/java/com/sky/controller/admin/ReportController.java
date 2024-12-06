package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Api("数据统计相关接口")
@RequestMapping("/admin/report")
@RestController
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 每天的收入
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("获取每天的收入数据")
    public Result<TurnoverReportVO> turnoverStatistic(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("营业额数据统计：{}，{}", begin, end);
        TurnoverReportVO turnoverStatistic = reportService.getTurnoverStatistic(begin, end);
        return Result.success(turnoverStatistic);
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("获取用户数据据")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户数据统计：{}，{}", begin, end);
        UserReportVO userStatistics = reportService.getUserStatistics(begin, end);
        return Result.success(userStatistics);
    }

}
