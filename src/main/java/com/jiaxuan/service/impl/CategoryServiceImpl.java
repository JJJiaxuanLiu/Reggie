package com.jiaxuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaxuan.common.CustomException;
import com.jiaxuan.domain.Category;
import com.jiaxuan.domain.Dish;
import com.jiaxuan.domain.Setmeal;
import com.jiaxuan.mapper.CategoryMapper;
import com.jiaxuan.service.CategoryService;
import com.jiaxuan.service.DishService;
import com.jiaxuan.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    /**
     * 根据id删除分类，在删除前要进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        //是否关联菜品,关联抛出异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int countDish = dishService.count(dishLambdaQueryWrapper);
        if(countDish>0){
            //已经关联菜品，抛出异常
            throw new CustomException("当前分类下关联了菜品，不能删除！");
        }

        //是否关联套餐，关联抛出异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int countSetmeal = setmealService.count(setmealLambdaQueryWrapper);
        if(countSetmeal>0){
            //已经关联套餐，抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删除！");
        }


        //正常删除
        super.removeById(id);

    }







}
