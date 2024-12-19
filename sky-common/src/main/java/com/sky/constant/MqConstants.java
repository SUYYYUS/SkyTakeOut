package com.sky.constant;

public class MqConstants {

    /**
     * 用于检查未支付订单的延迟队列和交换机
     */
    public static final String ORDER_DELAY_EXCHANGE = "order.delay.direct";
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";
}
