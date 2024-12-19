package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MqConfirmConfig implements ApplicationContextAware {

    //出现路由错误的时候，会触发下面这个
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        //配置回调
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                log.debug("收到消息的returnsCallback" , returnedMessage.getReplyCode(), returnedMessage.getMessage()
                        , returnedMessage.getExchange(),returnedMessage.getReplyText(), returnedMessage.getRoutingKey());
            }
        });
    }


}
