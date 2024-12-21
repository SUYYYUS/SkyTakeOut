package com.sky.utils;

import lombok.AllArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * 负责设置延迟时间的处理器
 */
@AllArgsConstructor
public class DelayMessageProcessor implements MessagePostProcessor {

    private final int delay;
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setDelay(delay);
        return message;
    }
}
