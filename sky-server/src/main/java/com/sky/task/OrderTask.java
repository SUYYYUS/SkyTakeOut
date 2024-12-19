package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 处理未支付超时订单
     */
    @Scheduled(cron = "0 * * * * ? ") //每分钟触发一次
    public void processOvertimeOrders(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //查询待付款状态的订单并且下单时间已经超过了15分钟
        List<Orders> byStatusAndOrderTime = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);

        if(!byStatusAndOrderTime.isEmpty() || byStatusAndOrderTime.size() > 0){
            for (Orders orders : byStatusAndOrderTime) {
                orders.setStatus(Orders.CANCELLED); //取消
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时，自动取消");
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void processDeliveryOrders(){
        log.info("处理一直处于派送中的订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> byStatusAndOrderTime = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);
        //不为空才要操作
        if(!byStatusAndOrderTime.isEmpty() || byStatusAndOrderTime.size() > 0){
        //使用MQ异步寻找人工处理
        rabbitTemplate.convertAndSend("order.direct", "red", byStatusAndOrderTime);
        }
    }

}
