package com.sky.controller.admin;

import com.sky.constant.RedisConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api("菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO){
        //不需要修改缓存，因为新增的默认停售，你改成启售的时候自然会清理缓存
        log.info("新增菜品");
        dishService.addDishWithFlavor(dishDTO);
        return Result.success();
    }


    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询");
        PageResult page = dishService.page(dishPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除：{}", ids);
        deleteAllCache(RedisConstant.CATEGORY_ID + "*");
        dishService.delete(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("查询菜品信息:{}",id);
        DishVO byId = dishService.getById(id);
        return Result.success(byId);
    }

    /**
     * 更新菜品信息
     * @param dishDTO
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("更新菜品信息：{}",dishDTO);
        dishService.update(dishDTO);
        deleteAllCache(RedisConstant.CATEGORY_ID + "*");
        return Result.success();
    }

    /**
     * 启售或停售菜品
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品出售状态")
    public Result startOrStop(@PathVariable int status, Long id){
        log.info("修改菜品出售状态：{}", id);
        dishService.startOrStop(status, id);
        deleteAllCache(RedisConstant.CATEGORY_ID + "*");
        return Result.success();
    }

    /**
     * 根据分类id查询其符合的菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类ID查询菜品")
    public Result getByCategoryId(Long categoryId){
        log.info("根据分类ID查询菜品：{}", categoryId);
        List<DishVO> byCategoryId = dishService.getByCategoryId(categoryId);
        return Result.success(byCategoryId);
    }

    public void deleteAllCache(String pattern){
        //获取所有分类id
        Set<String> keys = stringRedisTemplate.keys(pattern);
        //把所有分类id中的所有菜品缓存都删除
        stringRedisTemplate.delete(keys);
    }
}
