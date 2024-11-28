package com.sky.service.impl;

import com.sky.context.UserHolder;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 清空购物车
     */
    @Override
    public void cleanCart() {
        shoppingCartMapper.clean(UserHolder.getCurrentId());
    }

    /**
     * 新添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(UserHolder.getCurrentId());
        //判断当前想要加入的商品是否存在
        List<ShoppingCart> byUserIdAndId = shoppingCartMapper.getByUserIdAndId(shoppingCart);
        if (byUserIdAndId.size() > 0 && !byUserIdAndId.isEmpty()){
            //如果存在，数量加一即可
            ShoppingCart shoppingCart1 = byUserIdAndId.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
            shoppingCartMapper.update(shoppingCart1);
        }else {
            //不存在则新插入一条数据
            //判断添加的是菜品还是套餐
            if(shoppingCartDTO.getDishId() != null){
                //那就是菜品
                Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                //那就是套餐
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看用户的购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showCart() {
        ShoppingCart build = ShoppingCart.builder().id(UserHolder.getCurrentId())
                .build();
        List<ShoppingCart> byUserIdAndId = shoppingCartMapper.getByUserIdAndId(build);
        return byUserIdAndId;
    }

    /**
     * 删除商品
     * @param shoppingCartDTO
     */
    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        //获取用户id
        Long id = UserHolder.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(id);
        //先找到这条商品
        List<ShoppingCart> byUserIdAndId = shoppingCartMapper.getByUserIdAndId(shoppingCart);
        ShoppingCart cart = byUserIdAndId.get(0); //获取当前购物车
        //判断扣一后是否还有这条商品
        if(cart.getNumber() - 1 == 0){
            //扣减后没了，则直接删除这条数据
            shoppingCartMapper.deleteByUserIdAndId(shoppingCart);
        }else {
            //更改数量即可
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.update(cart);
        }
        //完成
    }
}
