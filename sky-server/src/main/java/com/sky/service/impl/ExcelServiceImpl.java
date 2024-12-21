package com.sky.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.sky.entity.Orders;
import com.sky.listener.ExcelOrdersListener;
import com.sky.mapper.OrderMapper;
import com.sky.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public void writeAllOrders() {

        String filename = "test1.xlsx";

        //获取写对象
        try (ExcelWriter excelWriter = EasyExcel.write(filename, Orders.class).build()) {

            //获取其中一页
            WriteSheet demo1 = EasyExcel.writerSheet("sheet1").build();

            ExcelWriter write = excelWriter.write(orderMapper.getAllOrders(), demo1);

            log.info("这两个相等吗》》》 {}", write.equals(excelWriter));

        }



    }

    @Override
    public void writeAllOrders2() {

    }

    @Override
    public void read1() {
        String filename = "test1.xlsx";

        ExcelReader reader = EasyExcel.read(filename, Orders.class, new ExcelOrdersListener()).build();
        ReadSheet sheet = EasyExcel.readSheet("demo1").build();
        ExcelReader read = reader.read(sheet);
    }
}
