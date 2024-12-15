package com.sky.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等性校验的自定义注解，支付订单
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckIdempotentPayOrder {

}
