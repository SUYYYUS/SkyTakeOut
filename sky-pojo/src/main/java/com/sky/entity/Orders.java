package com.sky.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders implements Serializable {

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_CONFIRMED = 2;
    public static final Integer CONFIRMED = 3;
    public static final Integer DELIVERY_IN_PROGRESS = 4;
    public static final Integer COMPLETED = 5;
    public static final Integer CANCELLED = 6;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private static final long serialVersionUID = 1L;

    @ExcelProperty("订单id")
    private Long id;

    @ExcelProperty("订单号")
    //订单号
    private String number;

    @ExcelProperty("订单状态")
    //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款
    private Integer status;

    @ExcelProperty("下单用户id")
    //下单用户id
    private Long userId;

    @ExcelProperty("地址id")
    //地址id
    private Long addressBookId;

    @ExcelProperty("下单时间")
    //下单时间
    private LocalDateTime orderTime;

    @ExcelProperty("结账时间")
    //结账时间
    private LocalDateTime checkoutTime;

    @ExcelProperty("支付方式")
    //支付方式 1微信，2支付宝
    private Integer payMethod;

    @ExcelProperty("支付状态")
    //支付状态 0未支付 1已支付 2退款
    private Integer payStatus;

    @ExcelProperty("金额")
    //实收金额
    private BigDecimal amount;

    @ExcelProperty("备注")
    //备注
    private String remark;

    @ExcelProperty("用户名")
    //用户名
    private String userName;

    @ExcelProperty("手机号")
    //手机号
    private String phone;

    @ExcelProperty("地址")
    //地址
    private String address;

    @ExcelProperty("收货人")
    //收货人
    private String consignee;

    @ExcelProperty("订单取消原因")
    //订单取消原因
    private String cancelReason;

    @ExcelProperty("订单拒绝原因")
    //订单拒绝原因
    private String rejectionReason;

    @ExcelProperty("订单取消时间")
    //订单取消时间
    private LocalDateTime cancelTime;

    @ExcelProperty("预计送达时间")
    //预计送达时间
    private LocalDateTime estimatedDeliveryTime;

    @ExcelProperty("配送状态")
    //配送状态  1立即送出  0选择具体时间
    private Integer deliveryStatus;

    @ExcelProperty("送达时间")
    //送达时间
    private LocalDateTime deliveryTime;

    @ExcelProperty("打包费")
    //打包费
    private int packAmount;

    @ExcelProperty("餐具数量")
    //餐具数量
    private int tablewareNumber;

    @ExcelProperty("餐具数量状态")
    //餐具数量状态  1按餐量提供  0选择具体数量
    private Integer tablewareStatus;
}
