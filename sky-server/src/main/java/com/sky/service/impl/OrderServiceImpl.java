package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstant;
import com.sky.context.UserHolder;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.RBloomFilterService;
import com.sky.utils.BloomFilterUtil;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.bouncycastle.crypto.agreement.jpake.JPAKEPrimeOrderGroup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RBloomFilterService rBloomFilterService;

    @Value("${sky.shop.address}")
    private String shopAddress;
    @Value("${sky.baidu.ak}")
    private String ak;

    /**
     * 添加订单数据
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO addOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //先判断各种业务异常，提高代码健壮性
        //1.地址簿是否为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        //2.查询当前用户的购物车
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.getByUserIdAndId(ShoppingCart.builder()
                .userId(UserHolder.getCurrentId())
                .build());
        if(shoppingCarts.size() == 0 || shoppingCarts.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //插入一条订单数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT); //待付款
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(UserHolder.getCurrentId());
        orderMapper.insert(orders); //插入数据库

        // 添加缓存到Redis中
        String key = RedisConstant.CACHE_ORDER + orders.getId();
        String value = JSON.toJSONString(orders);
        Boolean b = stringRedisTemplate.opsForValue().setIfAbsent(key, value);
        if (!b){
            throw new IllegalStateException("Something wrong");
        }

        // 添加订单id到布隆过滤器中
        rBloomFilterService.add(String.valueOf(orders.getId()));


        //插入n条订单详细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        //遍历购物车
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail(); //购物明细
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId()); //设置关联的订单id
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //删除购物车中的东西
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(UserHolder.getCurrentId());
        shoppingCartMapper.deleteByUserIdAndId(shoppingCart);
        //封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        //webSocket提醒
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", orders.getId());
        map.put("content", "订单号" + orders.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = UserHolder.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 查询历史订单
     * @param
     * @return
     */
    @Override
    public PageResult getHistoryOrders(int pageNum, int pageSize, Integer status) {
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setPage(pageNum);
        ordersPageQueryDTO.setPageSize(pageSize);
        List<OrderVO> orderVOList = new ArrayList<>();
        //获取当前用户id
        ordersPageQueryDTO.setUserId(UserHolder.getCurrentId());
        //进行分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //获取订单信息
        Page<Orders> historyOrders = orderMapper.getHistoryOrders(ordersPageQueryDTO);
        long total = historyOrders.getTotal();
        List<Orders> result = historyOrders.getResult();
        //添加返回类型
        for (Orders orders : result) {
            OrderVO orderVO = new OrderVO();
            List<OrderDetail> byOrderId = orderDetailMapper.getByOrderId(orders.getId());
            orderVO.setOrderDetailList(byOrderId);
            BeanUtils.copyProperties(orders, orderVO);
            orderVOList.add(orderVO);
        }
        //通过订单id获取订单详细信息
        PageResult pageResult = new PageResult();
        pageResult.setTotal(total);
        pageResult.setRecords(orderVOList);
        return pageResult;
    }

    /**
     * 查看订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderdetails(Integer id) {
        Orders orders = new Orders();
        //TODO 1.先通过布隆过滤器查询
        boolean check = rBloomFilterService.check(String.valueOf(id));
        if(!check){
            return null; //布隆过滤器找不到，说明还没有这个订单
        }
        //TODO 2.如果存在则进入缓存查询
        String s = stringRedisTemplate.opsForValue().get(RedisConstant.CACHE_ORDER + id);
        if(s == null){
            //TODO 3.缓存没有查询数据库，数据库存在，先存放入缓存中，再返回
            orders = orderMapper.getById(id);
            if(orders == null){
                //TODO 4.若数据库不存在，返回null，缓存中提示没有该东西
                return null;
            }else {
                String value = JSON.toJSONString(orders);
                stringRedisTemplate.opsForValue().setIfAbsent(RedisConstant.CACHE_ORDER + id, value);
            }
        }
        //查询详细信息
        List<OrderDetail> byOrderId = orderDetailMapper.getByOrderId(Long.valueOf(id));
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderDetailList(byOrderId);
        BeanUtils.copyProperties(orders, orderVO);
        //返回结果
        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancelOrder(Long id) {
        //先根据id查询该订单信息
        Orders orders = orderMapper.getById(Math.toIntExact(id));
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders orders1 = new Orders();
        orders1.setId(orders.getId());
        orders1.setStatus(Orders.CANCELLED);
        orders1.setRejectionReason("用户取消");
        orders1.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders1);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void oneMore(Long id) {
        //首先根据id获取原本的信息
        Orders orders = orderMapper.getById(Math.toIntExact(id));
        if (orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        //将订单的详细信息重新加入要用户的购物车中
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(UserHolder.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }
        //使用批量插入
        shoppingCartMapper.insertBatch(shoppingCartList);

    }


    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult getOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = new PageResult();
        List<OrderVO> orderVOList = new ArrayList<>();
        //获取订单信息
        Page<Orders> orders = orderMapper.getHistoryOrders(ordersPageQueryDTO);
        pageResult.setTotal(orders.getTotal());
        //获取订单集合
        List<Orders> ordersList = orders.getResult();
        for (Orders orders1 : ordersList) {
            //复制信息过去
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders1, orderVO);
            //把订单详细包含的菜品信息转为字符串保存
            String orderDishesStr = getOrderDishesStr(orders1);
            orderVO.setOrderDishes(orderDishesStr);
            orderVOList.add(orderVO);
        }
        pageResult.setRecords(orderVOList);
        return pageResult;
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO getOrdersStatusCount() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        //待接单的数量
        int count = orderMapper.getCountByStatus(Orders.TO_BE_CONFIRMED);
        orderStatisticsVO.setToBeConfirmed(count);
        //派送中数量
        int count1 = orderMapper.getCountByStatus(Orders.DELIVERY_IN_PROGRESS);
        orderStatisticsVO.setDeliveryInProgress(count1);
        //待派送数量
        int count2 = orderMapper.getCountByStatus(Orders.CONFIRMED);
        orderStatisticsVO.setConfirmed(count2);
        return  orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //根据id查询订单
        Orders orders1 = orderMapper.getById(Math.toIntExact(ordersConfirmDTO.getId()));
        //只有是待接单状态的订单商家才可以接单
        if (orders1 == null || !orders1.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED); //表示接单
        //修改信息
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //根据id查询订单信息
        Orders orders1 = orderMapper.getById(Math.toIntExact(ordersRejectionDTO.getId()));
        if (orders1 == null || !orders1.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //这里忽略支付的钱退回去，因为我们没有收到钱
        Orders orders = new Orders();
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setId(ordersRejectionDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        //修改状态
        orderMapper.update(orders);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders1 = orderMapper.getById(Math.toIntExact(ordersCancelDTO.getId()));
        if (orders1 == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //转化信息
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setStatus(Orders.CANCELLED);
        //修改订单信息
        orderMapper.update(orders);
        //完成
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orders1 = orderMapper.getById(Math.toIntExact(id));
        if (orders1 == null || !orders1.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
        //完成
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Integer id) {
        Orders orders1 = orderMapper.getById(id);
        if (orders1 == null || !orders1.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(Long.valueOf(id));
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now()); //送达时间
        orderMapper.update(orders);
        //完成
    }

    /**
     * 用户催单
     * @param id
     */
    @Override
    public void reminder(Integer id) {
        Orders orders1 = orderMapper.getById(id);
        if (orders1 == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号：" + orders1.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
        //完成
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }



    /**
     * 检查客户的收货地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }



}
