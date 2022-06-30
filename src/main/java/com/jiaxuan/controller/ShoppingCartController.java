package com.jiaxuan.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaxuan.common.BaseContext;
import com.jiaxuan.common.R;
import com.jiaxuan.domain.ShoppingCart;
import com.jiaxuan.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("shoppingCart:{}",shoppingCart);
        //没有传userid，需要指定用户id
        long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //查询当前菜品或者套餐是否已经在购物车中
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        if(dishId != null){
            //选择的是菜品，不是套餐
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());

        }else {
            //选择添加的是套餐，不是菜品
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        //存在，数量+1
        if(one != null){
            Integer number = one.getNumber();
            one.setNumber(number+1);
            shoppingCartService.updateById(one);
        }else {
            //不存在，新增一条数据
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }
        return  R.success(one);
    }


    /**
     * 购物车中菜品和套餐数量减少
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){

        log.info("shoppingCart:{}",shoppingCart);

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        //如果减少的为dish
        if(shoppingCart.getDishId() != null){
            queryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());

        }else {
            //如果减少的为setmeal
            queryWrapper.eq(shoppingCart.getSetmealId() != null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(queryWrapper);

        Integer number = one.getNumber();
        one.setNumber(number-1);

        shoppingCartService.updateById(one);

        //如果数量为0，删除这条数据
        if (one.getNumber() == 0){
            shoppingCartService.removeById(one);
        }

        return R.success(one);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车、、、");
        //根据userid查询list
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }


    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        log.info("清空购物车....");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车");
    }

}
