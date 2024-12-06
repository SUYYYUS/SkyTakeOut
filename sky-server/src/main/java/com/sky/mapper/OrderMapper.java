package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<Orders> getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Integer id);


    @Select("select count(*) from orders where status = #{status}")
    int getCountByStatus(Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{localDateTime}")
    List<Orders> getByStatusAndOrderTime(@Param("status") Integer status, @Param("localDateTime") LocalDateTime localDateTime);

    Double sumByMap(Map map);
}
