package com.sky.controller;

import com.sky.result.Result;
import com.sky.service.BloomFilterService;
import com.sky.service.RBloomFilterService;
import com.sky.utils.BloomFilterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import javax.annotation.Resource;

/**
 * 用于测试布隆过滤器
 */
@Slf4j
@RestController
@RequestMapping("/bloom")
public class BloomFilterController {

    @Resource
    private RBloomFilterService rBloomFilterService;

    @Resource
    private BloomFilterUtil bloomFilterConfig;

/*    @Resource
    private BloomFilterService bloomFilterService;
    @GetMapping("/add/{element}")
    public void add(@PathVariable("element") String element){
        bloomFilterService.add(element);
    }
    @GetMapping("/check/{element}")
    public String check(@PathVariable("element") String element){
        return bloomFilterService.check(element);
    }*/

    @GetMapping("/addR/{element}")
    public void addR(@PathVariable("element") String element){
        rBloomFilterService.add(element);
    }

    @GetMapping("/checkR/{element}")
    public Result checkR(@PathVariable("element") String element){
        String check = rBloomFilterService.check(element);
        return Result.success(check);
    }

    @GetMapping("/dilatationR")
    public void dilatationR(){
        bloomFilterConfig.dilatation();
    }
}

