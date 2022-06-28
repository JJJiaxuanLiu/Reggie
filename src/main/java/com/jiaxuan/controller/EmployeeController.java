package com.jiaxuan.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaxuan.common.R;
import com.jiaxuan.domain.Employee;
import com.jiaxuan.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         * 1页面提交的密码password进行md5加密处理
         * 2根据页面提交的username查询数据库
         * 3如果没有查询到则返回登录失败结果
         * 4密码比对，不一致则返回登录失败结果
         * 5查看员工状态，如果为已禁用，则返回员工已禁用结果
         * 6登录成功，将员工id存入session并返回登录成功结果
         */
        //1页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2根据页面提交的username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3如果没有查询到则返回登录失败结果
        if(emp == null) {
            return R.error("登录失败！");
        }

        //4密码比对，不一致则返回登录失败结果
        if(! password.equals(emp.getPassword())){
            return R.error("登录失败！");
        }

        //5查看员工状态，如果为已禁用，则返回员工已禁用结果(0禁用，1可用)
        if(emp.getStatus() == 0){
            return R.error("账号已禁用！");
        }

        //6登录成功，将员工id存入session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }


    /**
     * 员工退出操作
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理服务器端对应的员工session
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }


    /**
     * 新增员工操作
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("员工信息：{}",employee.toString());

        //设置初始密码123456，需要md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }


    /**
     * 员工分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pagesize={},name={}",page,pageSize,name);

        //分页构造器
        Page pageInfo = new Page(page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();
        //过滤条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,lambdaQueryWrapper);

        return R.success(pageInfo);
    }


    /**
     * 员工信息修改
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

        Long id = Thread.currentThread().getId();
        log.info("当前线程id：{}",id);

//        Long employId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(employId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }



    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息。。");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }else {
            return R.error("没有查询到对应员工信息");
        }

    }







}
