package com.sky.listener;

import com.sky.constant.MqConstants;
import com.sky.entity.Orders;
import com.sky.result.MultiDelayMessage;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.DelayMessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DelayOrderListener {
    @Autowired
    private OrderService orderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConstants.ORDER_DELAY_QUEUE, durable = "true"),
            exchange = @Exchange(value = MqConstants.ORDER_DELAY_EXCHANGE, delayed = "true", type = ExchangeTypes.DIRECT),
            key = MqConstants.ORDER_DELAY_ROUTING_KEY
    ))
    public void listenerOrderDelayMessage(MultiDelayMessage<Long> msg){
        //1.查询订单状态，判断是否已经支付
        Orders orders = orderService.getById(msg.getData());
        //2.如果已支付，直接结束
        if(orders == null || orders.getStatus() != 1){
            return;
        }
        //3.判断是否存在下一个延迟时间
        if(msg.hasNextDelay()){
            Long nextDelay = msg.removeNextDelay();
            //重新发消息
            rabbitTemplate.convertAndSend(MqConstants.ORDER_DELAY_EXCHANGE,
                    MqConstants.ORDER_DELAY_ROUTING_KEY,
                    msg,
                    new DelayMessageProcessor(nextDelay.intValue()));
        }else {
            //不存在，直接取消该订单
            orderService.cancelOrderBySystem(orders.getId());
        }
        //后续操作等...
    }
}
