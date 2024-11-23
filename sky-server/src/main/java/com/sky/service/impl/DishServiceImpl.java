package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.IdNullException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    public void addDishWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //向菜品表插入一条数据
        dishMapper.addDish(dish);
        Long id = dish.getId();

        //向口味表插入多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(!flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            });
        }
        dishFlavorMapper.insert(flavors);

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        long total = page.getTotal();
        List<DishVO> result = page.getResult();
        PageResult pageResult = new PageResult();
        pageResult.setTotal(total);
        pageResult.setRecords(result);
        return pageResult;
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        //判断当前菜品是否可以删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus().equals(StatusConstant.ENABLE)){
                //在售卖中不可删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //被套餐关联了也不可以删除
        List<Long> setmealDishIdsByDishIds = setmealDishMapper.getSetmealDishIdsByDishIds(ids);
        if(!setmealDishIdsByDishIds.isEmpty() && setmealDishIdsByDishIds != null){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品
        dishMapper.delete(ids);
        //删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        if(id == null){
            throw new IdNullException(MessageConstant.ID_NULL);
        }
        DishVO dishVO = new DishVO();
        Dish dish = dishMapper.getById(id);
        //直接复制
        BeanUtils.copyProperties(dish, dishVO);
        //查询口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(flavors);
        //返回结果
        return dishVO;
    }

    /**
     * 更新菜品信息
     * @param dishDTO
     */
    @Override
    public void update(DishDTO dishDTO) {
        //复制
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //更新菜品
        dishMapper.update(dish);
        //更新菜品口味
        //先删除全部口味
        dishFlavorMapper.deleteByDishIds(Collections.singletonList(dishDTO.getId()));
        //再插入
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors.size() > 0 && flavors != null){
            flavors.stream().forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
        }
        dishFlavorMapper.insert(flavors);
        //完成
    }

    /**
     * 修改菜品出售情况
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(int status, Long id) {
        if(id == null){
            throw new IdNullException(MessageConstant.ID_NULL);
        }
        //创建对象
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        //进行更改
        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<DishVO> getByCategoryId(Long categoryId) {
        List<DishVO> byCategoryId = dishMapper.getByCategoryId(categoryId);
        return byCategoryId;
    }


}
