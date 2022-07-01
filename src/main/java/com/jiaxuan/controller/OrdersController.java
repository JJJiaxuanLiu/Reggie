package com.jiaxuan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaxuan.common.R;
import com.jiaxuan.domain.Orders;
import com.jiaxuan.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/order")
@Slf4j
/**
 * 订单
 */
public class OrdersController {

    @Autowired
    private OrdersService ordersService;


    /**
     * 用户提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){

        ordersService.submit(orders);
        return R.success("下单成功！");
    }


    /**
     * 订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize){
        log.info("page:{}",page);
        log.info("pageSize:{}",pageSize);

        Page page1 = ordersService.page(page, pageSize);

        return R.success(page1);
    }


}
