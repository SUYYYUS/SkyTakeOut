package com.sky.constant;

public class CheckIdempotentConstant {

    //发送支付订单请求时 校验请求头中的token
    public static final String PAY_ORDER_TOKEN = "pay_order_token";
    public static final String PAY_ORDER_TOKEN_REDIS = "pay:order:token:";
    public static final String ADD_ORDER_ORDER_REDIS = "add:order:token:";

}
