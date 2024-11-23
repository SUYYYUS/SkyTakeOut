package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //先插入套餐表
        Long id = setmealMapper.insert(setmeal);
        //再插入套餐菜品表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //先插入套餐id
        if(setmealDishes.size() > 0 && setmealDishes != null){
            setmealDishes.stream().forEach(setmealDish -> {
                setmealDish.setSetmealId(id);
            });
        }
        setmealDishMapper.insert(setmealDishes);
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);

        long total = page.getTotal();
        List<SetmealVO> result = page.getResult();
        for (SetmealVO setmealVO : result) {
            setmealVO.setCategoryName(categoryMapper.getById(setmealVO.getCategoryId()).getName());
        }
        //创建返回对象
        PageResult pageResult = new PageResult();
        pageResult.setTotal(total);
        pageResult.setRecords(result);
        //返回数据
        return pageResult;
    }
}
