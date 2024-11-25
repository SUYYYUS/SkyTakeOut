package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.IdNullException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        setmealMapper.insert(setmeal);
        //再插入套餐菜品表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //先插入套餐id
        if(setmealDishes.size() > 0 && setmealDishes != null){
            setmealDishes.stream().forEach(setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
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

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public Result getById(Long id) {
        //确保id不为空
        if(id == null){
            throw new IdNullException(MessageConstant.ID_NULL);
        }
        //查询套餐基本信息
        Setmeal byId = setmealMapper.getById(id);
        //查询套餐包含哪些菜品
        List<SetmealDish> bySetmealId = setmealDishMapper.getBySetmealId(id);
        //创建返回对象
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(byId, setmealVO);
        setmealVO.setSetmealDishes(bySetmealId);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //1.修改套餐的信息
        setmealMapper.update(setmeal);
        //2.修改套餐含有的菜品信息
        //先删除原来的信息
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        //再重新插入
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes.size() > 0 && setmealDishes != null){
            setmealDishes.stream().forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
        }
        setmealDishMapper.insert(setmealDishes);
        //完成
    }

    /**
     * 套餐的启售停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(int status, Long id) {
        //判断id不会空
        if(id == null){
            throw new IdNullException(MessageConstant.ID_NULL);
        }
        //创建对象
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        //进行修改
        setmealMapper.update(setmeal);
        //完成
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteSetmeals(List<Long> ids) {
        //先判断是否可以删除：通过查询有无状态为启售，为启售则不可以删除
        int i = setmealMapper.status1(ids);
        if (i > 0){
            //说明有正在售卖的套餐，不能删除，抛出异常
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }else {
            //1.在套餐表中删除
            setmealMapper.deleteSetmeals(ids);
            //2.在套餐菜品表中相关信息也要删除
            setmealDishMapper.deleteBySetmealIds(ids);
        }
        //完成
    }

//
//    @Autowired
//    private MinioClient minioClient;
//    public void test() throws Exception{
//        System.out.println(minioClient);
//
//        String bucketName = "skytakeout";
//        boolean b = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
//        if(!b){
//            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
//        }
//
//    }

}
