package com.jiaxuan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaxuan.domain.Setmeal;
import com.jiaxuan.dto.SetmealDto;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    //操作setmeal和setmeal_dish两张表，新增套餐
    void saveWithDish(SetmealDto setmealDto);

    //操作setmeal和setmeal_dish两张表，通过id查询，用于修改套餐信息回显
    SetmealDto getByIdWithDishes(Long id);

    //操作setmeal和setmeal_dish两张表,将回显的数据进行更新
    void updateWithDishes(SetmealDto setmealDto);

    //操作setmeal和setmeal_dish两张表，根据setmeal id删除套餐和对应dish
    void removeWithDishes(List<Long> ids);

    //更新套餐售卖状态
    void updateStatus(int status, List<Long> ids);
}
