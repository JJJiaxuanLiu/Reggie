package com.jiaxuan.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaxuan.domain.OrderDetail;
import com.jiaxuan.mapper.OrderDetailMapper;
import com.jiaxuan.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
