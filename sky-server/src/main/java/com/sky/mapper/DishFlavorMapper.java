package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    //新增菜品的口味
    void insert(List<DishFlavor> flavors);

    //删除菜品相连的口味数据
    void deleteByDishIds(List<Long> ids);

    //通过菜品id获取其相关的口味
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

}
