package com.jiaxuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.jiaxuan.common.BaseContext;
import com.jiaxuan.common.CustomException;
import com.jiaxuan.domain.*;
import com.jiaxuan.dto.OrdersDto;
import com.jiaxuan.mapper.OrdersMapper;
import com.jiaxuan.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     */
    @Transactional
    public void submit(Orders orders) {

        //获取用户id
        long userId = BaseContext.getCurrentId();

        //查询购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        if(shoppingCartList == null){
            throw new CustomException("购物车为空，无法下单！");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if(addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        //遍历shoppingcartlist，算总金额，并给orderdetial赋值
        long ordersNumber = IdWorker.getId();//生成订单号
        AtomicInteger amount = new AtomicInteger(0); //原子类型，保证线程安全

        List<OrderDetail> orderDetailList = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(ordersNumber);
            orderDetail.setName(item.getName());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        //向orders表中插入数据，一条
        orders.setNumber(String.valueOf(ordersNumber));
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);  //待派送
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName()==null ? "" : addressBook.getProvinceName())
                +(addressBook.getCityName()==null ? "" : addressBook.getCityName())
                +(addressBook.getDistrictName()==null ? "" : addressBook.getDistrictName())
                +(addressBook.getDetail()==null ? "" : addressBook.getDetail())
        );

        this.save(orders);

        //向orderdetial中插入多条数据
        orderDetailService.saveBatch(orderDetailList);

        //清空购物车
        shoppingCartService.remove(queryWrapper);

    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     */
    public Page page(int page, int pageSize) {
        //获取当前用户id
        long userId = BaseContext.getCurrentId();

        //分页构造器
        Page<Orders> ordersPage = new Page<>(page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //查询条件
        queryWrapper.eq(Orders::getUserId,userId);
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //分页查询
        this.page(ordersPage,queryWrapper);

        //对象拷贝，order对象中没有菜品名称信息，所以需要orderdto的orderdetial中的list集合中获取名称信息
        Page<OrdersDto> ordersDtoPage = new Page<>();
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        List<Orders> records = ordersPage.getRecords();

        List<OrdersDto> ordersDtoList = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);

            //通过orderid将orderdetial的list集合加入orderdto
            String orderId = item.getNumber();
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId,orderId);
            List<OrderDetail> list = orderDetailService.list(queryWrapper1);

            ordersDto.setOrderDetails(list);

            return ordersDto;

        }).collect(Collectors.toList());


        ordersDtoPage.setRecords(ordersDtoList);
        return ordersDtoPage;
    }

    /**
     * 后端订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page backendPage(int page, int pageSize) {
        //分页构造器
        Page<Orders> ordersPage = new Page<>(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //查询条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //分页查询
        this.page(ordersPage,queryWrapper);
        return ordersPage;
    }

    /**
     * 再来一单
     * @param orders
     */
    @Override
    public void again(Orders orders) {
        //获取order的id，通过id将订单中的菜品或套餐信息加入购物车
        log.info("orders:{}",orders);

        Orders orders1 = this.getById(orders);
        String orderNumber = orders1.getNumber();

        //在orderdetial中通过订单号查找
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,orderNumber);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setName(item.getName());
            shoppingCart.setImage(item.getImage());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setDishId(item.getDishId()==null ? null : item.getDishId());
            shoppingCart.setSetmealId(item.getSetmealId()==null ? null : item.getSetmealId());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());


        shoppingCartService.saveBatch(shoppingCartList);

    }


}
