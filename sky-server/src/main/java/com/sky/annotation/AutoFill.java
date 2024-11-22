package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行公共字段自动填充
 */
@Target(ElementType.METHOD) //注解加在方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //指定当前数据库操作类型：update，insert
    OperationType value();

}
