package com.sky.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等性校验的自定义注解，提交订单
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckIdempotentAddOrder {
    int second = 3;
}
