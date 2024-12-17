package com.sky.service;


import com.sky.utils.BloomFilterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 基于redis的布隆过滤器
 */
@Service
@Slf4j
public class RBloomFilterService {

    /**
     * 私有的不可变的bloomFilter对象
     */
    @Resource
    private BloomFilterUtil bloomFilterUtil;

    /**
     * 向过滤器添加元素
     * @param element
     */
    public void add(String element){
        bloomFilterUtil.rBloomFilter.add(element);
    }

    /**
     * 检查过滤器中是否存在该元素
     * @param element
     * @return
     */
    public boolean check(String element){
        log.info("序号：{}", bloomFilterUtil.RBloomFilterSequence);
        log.info("元素个数：{}", bloomFilterUtil.rBloomFilter.count());
        log.info("期望插入数：{}", bloomFilterUtil.rBloomFilter.getExpectedInsertions());
        log.info("假阳性概率：{}", bloomFilterUtil.rBloomFilter.getFalseProbability());
        return bloomFilterUtil.rBloomFilter.contains(element);

    }


}
