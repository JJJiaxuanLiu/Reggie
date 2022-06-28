package com.jiaxuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaxuan.domain.Dish;
import com.jiaxuan.domain.DishFlavor;
import com.jiaxuan.dto.DishDto;
import com.jiaxuan.mapper.DishMapper;
import com.jiaxuan.service.DishFlavorService;
import com.jiaxuan.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品并且保存口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存数据到dish表中
        this.save(dishDto);

        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) ->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存数据到dish_flavor表中
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }

    /**
     * 根据菜品id查询对应的菜品信息和口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询基本信息，从dish表中
        Dish dish = this.getById(id);

        //查询口味信息，从dish_flavor表中
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(dishFlavorList);

        return dishDto;
    }


    /**
     * 更新菜品和口味信息
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品信息(dish table)
        this.updateById(dishDto);

        //更新口味信息(dish_flavor table)
        //1 删除之前的口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //2 增加修改的口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //和saveWithFlavor操作一样，dish_flavor中没有dish_id字段,需要手动添加
        flavors = flavors.stream().map((item) -> {
            //使用雪花算法将dish_flavor的id重新设置，避免id重复
            //item.setId(IdWorker.getId());
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);


    }

    /**
     * 根据id删除dish和dish_flavor两张表的菜品
     * @param ids
     */
    @Transactional
    @Override
    public void deleteByIdWithFlavor(String ids) {

        String[] split = ids.split(",");
        for (String id:split) {
            //删除dish表中的菜品
            this.removeById(Long.parseLong(id));
            //根据dish_id删除dish_flavor表中的数据
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId,id);
            dishFlavorService.remove(queryWrapper);
        }



    }


    /**
     * 更新菜品起售停售状态
     * @param status
     * @param ids
     */
    @Override
    public void updateDishStatus(int status, String ids) {
        String[] idList = ids.split(",");
        for (String id : idList){
            Dish dish = new Dish();
            dish.setId(Long.parseLong(id));
            dish.setStatus(status);
            this.updateById(dish);
        }
    }


}
