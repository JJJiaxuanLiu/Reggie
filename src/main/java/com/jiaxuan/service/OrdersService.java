package com.jiaxuan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaxuan.domain.Orders;

public interface OrdersService extends IService<Orders> {

    /**
     * 用户提交订单
     * @param orders
     */
    void submit(Orders orders);


    /**
     * 分页查询
     * @param page
     * @param pageSize
     */
    Page page(int page, int pageSize);
}
