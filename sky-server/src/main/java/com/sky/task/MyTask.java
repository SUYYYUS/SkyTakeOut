package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyTask {


//    @Scheduled(cron = "0/5 * * * * ?")
//    public void test(){
//        log.info("现在是：{}", LocalDateTime.now());
//    }
}
