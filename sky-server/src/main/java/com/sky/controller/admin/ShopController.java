package com.sky.controller.admin;

import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api("商店相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 修改店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("修改店铺营业状态")
    public Result updateShopStatus(@PathVariable Integer status){
        log.info("修改店铺营业状态：{}", status);
        stringRedisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS, String.valueOf(status));
        return Result.success();
    }

    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result getShopStatus(){
        Integer s = Integer.valueOf(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS)));
        log.info("店铺营业状态为：{}", s == 1 ? "营业种" : "关门中");
        return Result.success(s);
    }

}
