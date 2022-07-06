package com.jiaxuan.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaxuan.common.R;
import com.jiaxuan.domain.Category;
import com.jiaxuan.domain.Dish;
import com.jiaxuan.domain.DishFlavor;
import com.jiaxuan.dto.DishDto;
import com.jiaxuan.service.CategoryService;
import com.jiaxuan.service.DishFlavorService;
import com.jiaxuan.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品方法
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        //删除菜品分类缓存
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("新增菜品成功！");
    }


    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //查询条件，使用模糊查询name关键字
        queryWrapper.like(name != null, Dish::getName,name);

        //查询倒叙
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(dishPage,queryWrapper);

        //对象拷贝
        Page<DishDto> dishDtoPage = new Page<>();
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");

        List<Dish> records = dishPage.getRecords();


        List<DishDto> dishDtoList = records.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();

            dishDto.setCategoryName(categoryName);

            return dishDto;

        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);
        return R.success(dishDtoPage);
    }

    /**
     * 菜品id查询对应的菜品信息和口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);

        return R.success(byIdWithFlavor);
    }


    /**
     * 更新菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        //更新dish和dish_flavor两张表
        dishService.updateWithFlavor(dishDto);

        //删除某个分类下的菜品数据缓存
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("更新菜品成功！");
    }


    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String ids){
        log.info("删除菜品和口味");
        dishService.deleteByIdWithFlavor(ids);

        return R.success("删除菜品成功!");
    }


    /**
     * 更新菜品起售停售状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> modifyDishStatus(@PathVariable int status, String ids){

        dishService.updateDishStatus(status,ids);
        //清理缓存
        String key = "";
        String[] strings = ids.split(",");
        for (String id : strings) {
            Dish dish = dishService.getById(id);
            key = "dish_"+dish.getCategoryId()+"_1";
            redisTemplate.delete(key);
        }
        return R.success("菜品状态修改成功！");
    }

//    /**
//     * 根据条件查询对应菜品数据
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        //构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId,dish.getCategoryId());
//        //status==1表示起售状态的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//        //排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> dishList = dishService.list(queryWrapper);
//        return R.success(dishList);
//    }





    /**
     * 根据条件查询对应菜品数据，因为用户页面要展示口味信息，返回dish不能满足，改为返回dishdto的list集合
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        List<DishDto> dishDtoList = null;
        //在redis中查询缓存
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        //如果redis中存在缓存，直接返回
        if(dishDtoList != null){
            return R.success(dishDtoList);
        }
        //如果redis中没有缓存，查询数据库，保存缓存

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId,dish.getCategoryId());
        //status==1表示起售状态的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(queryWrapper);

        //将dish中其他属性复制到dishdto中，根据dishid在dishflavor中查询对应口味信息
        dishDtoList = dishList.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            //根据dishid在dishflavor中查询对应口味信息
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(dishId != null,DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(wrapper);

            //将查询到的dishflavor的list集合放入dishdto中
            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());

        //如果redis中没有缓存，查询数据库，保存缓存(60min)
        redisTemplate.opsForValue().set(key,dishDtoList,60L, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }














}
