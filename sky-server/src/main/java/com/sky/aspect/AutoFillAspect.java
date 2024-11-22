package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.UserHolder;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)") //mapper的所有类所有的方法，同时要满足方法上加了这个自定义注解
    public void autoFillPointCut(){
    }

    //定义一个通知，前置通知，在操作之前为它赋上值
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段自动填充...");

        //获取当前被拦截的方法的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //方法签名对象
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class); //获取注解对象
        OperationType value = annotation.value(); //获得操作类型
        //获取方法的参数实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null){
            return;
        }
        Object arg = args[0];
        //准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long id = UserHolder.getCurrentId();
        //根据不同操作类型进行赋值
        if(value == OperationType.INSERT){
            //反射获得
            Method setCreateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            //赋值
            setCreateTime.invoke(arg, now);
            setUpdateTime.invoke(arg, now);
            setCreateUser.invoke(arg, id);
            setUpdateUser.invoke(arg, id);
        } else if (value == OperationType.UPDATE) {
            Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            setUpdateUser.invoke(arg, id);
            setUpdateTime.invoke(arg, now);
        }
    }


}
