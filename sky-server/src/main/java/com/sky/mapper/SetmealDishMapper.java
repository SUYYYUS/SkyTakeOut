package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    //根据菜品id查询菜品存在的套餐的id
    List<Long> getSetmealDishIdsByDishIds(List<Long> DishIds);

    //插入新的套餐菜品关系
    void insert(List<SetmealDish> setmealDishes);

    //根据套餐id查询含有的菜品
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

    //根据套餐id删除信息
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySermealId(Long setmealId);
}
