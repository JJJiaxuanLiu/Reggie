package com.jiaxuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaxuan.common.CustomException;
import com.jiaxuan.domain.Setmeal;
import com.jiaxuan.domain.SetmealDish;
import com.jiaxuan.dto.SetmealDto;
import com.jiaxuan.mapper.SetmealMapper;
import com.jiaxuan.service.SetmealDishService;
import com.jiaxuan.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {

        //保存套餐基本信息，setmeal表，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //因为没有传setmeal_id所以需要遍历增加
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套菜菜品信息，setmeal_dish表，执行insert操作
        setmealDishService.saveBatch(setmealDishes);

    }


    /**
     * 操作setmeal和setmeal_dish两张表，通过id查询，用于修改套餐信息回显
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDishes(Long id) {
        //查询基本信息
        Setmeal setmeal = this.getById(id);

        //查询口味信息，从dish_flavor表中
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(setmealDishList);

        return setmealDto;
    }

    /**
     * 操作setmeal和setmeal_dish两张表,将回显的数据进行更新
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDishes(SetmealDto setmealDto) {

        //更新套餐基本信息
        this.updateById(setmealDto);


        //更新菜品信息
        //1删除之前的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //2添加现在的菜品
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        //因为没有传setmeal_id所以需要遍历增加
        setmealDishList.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishList);

    }

    /**
     * 操作setmeal和setmeal_dish两张表，根据setmeal id删除套餐和对应dish
     * 套餐状态为售卖中不可以删除(1=售卖中, 0=停售)
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDishes(List<Long> ids) {
        //select* from setmeal where id in(1,2,3) and status=1
        //判断套餐售卖状态
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        //不能删除抛出业务异常
        if (count>0){
            throw new CustomException("套餐正在售卖中，不能删除！");
        }

        //删除setmeal中的数据
        this.removeByIds(ids);

        //删除setmeal_dish中的数据
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper1);

    }

    /**
     * 更新套餐售卖状态
     * @param status
     * @param ids
     */
    @Override
    public void updateStatus(int status, List<Long> ids) {

        ids.stream().map((item) -> {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(item);
            setmeal.setStatus(status);
            this.updateById(setmeal);
            return setmeal;
        }).collect(Collectors.toList());


    }


}
