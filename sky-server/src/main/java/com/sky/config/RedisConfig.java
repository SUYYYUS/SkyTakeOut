package com.sky.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Bean
    public RedissonClient redissonClient(){
        //配置类
        Config config = new Config();
        //添加redis地址，这里添加的是单节点，还没有做集群
        config.useSingleServer().setAddress("redis://192.168.223.142:6379").setPassword("123456").setDatabase(2);
        return Redisson.create(config);
    }

}
