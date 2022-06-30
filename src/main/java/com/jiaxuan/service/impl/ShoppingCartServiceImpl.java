package com.jiaxuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaxuan.domain.ShoppingCart;
import com.jiaxuan.mapper.ShoppingCartMapper;
import com.jiaxuan.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
