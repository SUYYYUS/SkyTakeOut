package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;

import java.util.List;


public interface SetmealService {
    void addSetmeal(SetmealDTO setmealDTO);

    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    Result getById(Long id);

    void update(SetmealDTO setmealDTO);

    void startOrStop(int status, Long id);

    void deleteSetmeals(List<Long> ids);
}
