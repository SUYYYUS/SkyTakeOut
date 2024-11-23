package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    //根据菜品id查询菜品存在的套餐的id
    List<Long> getSetmealDishIdsByDishIds(List<Long> DishIds);

    //插入新的套餐菜品关系
    void insert(List<SetmealDish> setmealDishes);
}
