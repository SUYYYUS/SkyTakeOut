package com.sky.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.sky.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义监听器读取数据
 */
@Component
@Slf4j
public class ExcelOrdersListener implements ReadListener<Orders> {
    private int count = 5;

    private List<Orders> list = new ArrayList<>(count);



    /**
     * 每读一行数据，都会调用这个方法
     * @param orders
     * @param analysisContext
     */
    @Override
    public void invoke(Orders orders, AnalysisContext analysisContext) {
        //将读取到的数据添加到集合中
        list.add(orders);
        if(list.size() >= count){
            log.info("模拟操作数据，往数据库写入数据一次");
            list = new ArrayList<>(count); //清理缓存
        }
    }

    /**
     * 读完这个excel文件后，再调用这个方法
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(list.size() > 0){
            log.info("模拟操作数据，最后剩余几条数据，操作一下就好了====================");
        }
    }
}
