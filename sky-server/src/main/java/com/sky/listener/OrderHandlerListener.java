package com.sky.listener;

import com.alibaba.fastjson.JSON;
import com.sky.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class OrderHandlerListener {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order.queue", durable = "true"),
            exchange = @Exchange(value = "order.direct", type = ExchangeTypes.DIRECT),
            key = {"red", "blue"}
    ))
    public void listenOrderDelivering(@NotNull List<Orders> byStatusAndOrderTime){
        //监听到的信息，放入redis中
        for (Orders orders : byStatusAndOrderTime) {
            stringRedisTemplate.opsForList().leftPush("exception:order", JSON.toJSONString(orders));
        }

    }


}
