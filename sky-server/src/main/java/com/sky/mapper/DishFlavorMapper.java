package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    void insert(List<DishFlavor> flavors);

    //删除菜品相连的口味数据
    void deleteByDishIds(List<Long> ids);
}
