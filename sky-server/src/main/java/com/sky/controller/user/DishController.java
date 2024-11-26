package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        List<DishVO> list = new ArrayList<>();
        //生成key
        String key = RedisConstant.CATEGORY_ID + categoryId;
        //从redis中查询数据
        List<String> list1 = stringRedisTemplate.opsForList().range(key, 0, -1);
        //存在则直接返回
        if(list1 != null && list1.size() > 0){
            for (String s : list1) {
                DishVO dishVO = JSON.parseObject(s, DishVO.class);
                list.add(dishVO);
            }
        }else {
            //不存在查询数据库，先添加缓存，之后返回
            Dish dish = new Dish();
            dish.setCategoryId(categoryId);
            dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

            list = dishService.listWithFlavor(dish);
            List<String> list2 = new ArrayList<>();
            for (DishVO dishVO : list) {
                String jsonString = JSON.toJSONString(dishVO);
                list2.add(jsonString);
            }
            stringRedisTemplate.opsForList().leftPushAll(key,list2);
        }

        return Result.success(list);
    }

}
