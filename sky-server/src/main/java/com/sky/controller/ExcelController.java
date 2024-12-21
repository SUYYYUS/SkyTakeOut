package com.sky.controller;

import com.sky.result.Result;
import com.sky.service.ExcelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/excel")
@Slf4j
@Api("用于学习EasyExcel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;


    @GetMapping("/write1")
    public Result write1(){
        log.info("test1写数据执行");
        excelService.writeAllOrders();
        return Result.success();
    }

    @GetMapping("/write2")
    public Result write2(){
        log.info("test2写数据执行");
        excelService.writeAllOrders2();
        return Result.success();
    }

    @GetMapping("/read1")
    public Result read1(){
        log.info("read1执行");
        excelService.read1();
        return Result.success();
    }


}
