package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api("用户端订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * 新增订单
     * @param ordersSubmitDTO
     * @return
     */
    @ApiOperation("新增订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> addOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.addOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询历史订单
     * @param
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查看历史订单")
    public Result<PageResult> getHistoryOrders(int page, int pageSize, Integer status){

        log.info("历史订单查询：{}，{}，{}", page,pageSize,status);
        PageResult historyOrders = orderService.getHistoryOrders(page,pageSize,status);
        return Result.success(historyOrders);
    }

    /**
     * 查看订单详情
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查看订单详情")
    public Result getOrderDetails(@PathVariable Integer id){
        log.info("查看订单详情：{}",id);
        OrderVO orderdetails = orderService.getOrderdetails(id);
        return Result.success(orderdetails);
    }

    /**
     * 用户取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancelOrder(@PathVariable Long id){
        log.info("取消订单：{}",id);
        orderService.cancelOrder(id);
        return Result.success();
    }


    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result oneMore(@PathVariable Long id){
        log.info("再来一单：{}", id);
        orderService.oneMore(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Integer id){
        log.info("用户催单：{}", id);
        orderService.reminder(id);
        return Result.success();
    }

}
