package com.jiaxuan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaxuan.domain.Dish;
import com.jiaxuan.dto.DishDto;



public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish，dish_flavor
    void saveWithFlavor(DishDto dishDto);

    //根据菜品id查询对应的菜品信息和口味信息,需要操作两张表：dish，dish_flavor
    DishDto getByIdWithFlavor(Long id);

    //更新dish和dish_flavor两张表
    void updateWithFlavor(DishDto dishDto);

    //根据id删除dish和dish_flavor两张表的菜品
    void deleteByIdWithFlavor(String ids);

    //更新菜品起售停售状态
    void updateDishStatus(int status,String ids);




}
