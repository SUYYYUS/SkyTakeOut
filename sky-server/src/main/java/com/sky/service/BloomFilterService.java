package com.sky.service;

import com.sky.utils.BloomFilterUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 基于JVM的布隆过滤器
 */
@Service
public class BloomFilterService {

    /**
     * 私有的不可变的bloomFilter对象
     */
/*    @Resource
    private BloomFilterUtil bloomFilterUtil;*/

    /**
     * 向过滤器添加元素
     * @param element
     */
/*    public void add(String element){
        bloomFilterUtil.bloomFilter.put(element);
    }*/

    /**
     * 检查过滤器中是否存在该元素
     * @param element
     * @return
     */
/*    public String check(String element){
        if(bloomFilterUtil.bloomFilter.mightContain(element)){
            return element + "可能存在于集合中";
        }else {
            return element + "不可能存在集合中";
        }
    }*/


}
