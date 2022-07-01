package com.jiaxuan.dto;

import com.jiaxuan.domain.OrderDetail;
import com.jiaxuan.domain.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;

	
}
