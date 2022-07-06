package com.jiaxuan.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaxuan.common.R;
import com.jiaxuan.domain.Category;
import com.jiaxuan.domain.Setmeal;
import com.jiaxuan.dto.DishDto;
import com.jiaxuan.dto.SetmealDto;
import com.jiaxuan.service.CategoryService;
import com.jiaxuan.service.SetmealDishService;
import com.jiaxuan.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true) //删除缓存
    public R<String> save(@RequestBody SetmealDto setmealDto){

        setmealService.saveWithDish(setmealDto);
        //删除某个分类下的套餐数据缓存
        String key = "setmeal_"+setmealDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("新增套餐成功！");
    }


    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //分页构造器
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //执行分页查询
        setmealService.page(setmealPage, queryWrapper);

        //拿到的数据没有categoryname属性，需要使用拷贝到dishDto中使用
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        List<Setmeal> setmealList = setmealPage.getRecords();

        List<SetmealDto> setmealDtoList = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            //根据item中的categoryid得到categoryname，set进入setmealDto中
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            setmealDto.setCategoryName(categoryName);

            return setmealDto;

        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);
        return R.success(setmealDtoPage);
    }


    /**
     * 通过id查询套餐信息，用于套餐修改功能的回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getByIdWithDishes(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDishes(id);

        return R.success(setmealDto);
    }


    /**
     * 修改回显后，更新信息并保存
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){

        //更新setmeal和setmeal_dish两张表
        setmealService.updateWithDishes(setmealDto);

        //删除某个分类下的套餐数据缓存
        String key = "setmeal_"+setmealDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("套餐更新成功！");
    }


    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)  //删除所有套餐缓存
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids{}",ids);
        setmealService.removeWithDishes(ids);
        return R.success("套餐删除成功");
    }


    /**
     * 更新套餐售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache",allEntries = true)   //删除所有套餐缓存
    public R<String> updateStatus(@PathVariable int status, @RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.updateStatus(status,ids);

        //删除某个分类下的套餐数据缓存
        String key = "";
        for (Long id : ids) {
            Setmeal setmeal = setmealService.getById(id);
            key = "setmeal_"+setmeal.getCategoryId()+"_1";
            redisTemplate.delete(key);
        }


        return R.success("套餐售卖状态修改成功！");
    }



    /**
     * 根据条件查询对应套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")  //查看缓存中是否存在，不存在则放入缓存
    public R<List<Setmeal>> list(Setmeal setmeal){

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus,1);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);


        return R.success(list);

    }

}
