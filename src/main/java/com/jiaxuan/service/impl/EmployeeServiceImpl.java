package com.jiaxuan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaxuan.domain.Employee;
import com.jiaxuan.mapper.EmployeeMapper;
import com.jiaxuan.service.EmployeeService;
import org.springframework.stereotype.Service;


@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
