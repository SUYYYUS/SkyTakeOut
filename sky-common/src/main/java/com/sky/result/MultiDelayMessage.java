package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultiDelayMessage<T> {
    //消息体
    private T data;

    //记录延迟时间的集合
    private List<Long> delayMillis;

//    public MultiDelayMessage(T data, List<Long> delayMillis){
//        this.data = data;
//        this.delayMillis = delayMillis;
//    }

    public static <T> MultiDelayMessage<T> of (T data, Long[] delayMillis){
        return new MultiDelayMessage<>(data, Arrays.stream(delayMillis)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public Long removeNextDelay(){
        System.out.println(delayMillis.size());

        return delayMillis.remove(0);
    }

    public boolean hasNextDelay(){
        return !delayMillis.isEmpty();
    }

}
