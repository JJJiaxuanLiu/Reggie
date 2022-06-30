package com.jiaxuan.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jiaxuan.common.BaseContext;
import com.jiaxuan.common.R;
import com.jiaxuan.domain.AddressBook;
import com.jiaxuan.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}",addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }


    /**
     * 查询用户的全部地址
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        //根据用户id查询
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}",addressBook);

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId,addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        return R.success(addressBookService.list(queryWrapper));
    }


    /**
     * 设置默认地址
     * 1为默认，0为非默认
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @Transactional
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        log.info("addressBook:{}", addressBook);
        //将该用户所有地址isdefault字段都改为0
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        updateWrapper.set(AddressBook::getIsDefault,0);
        addressBookService.update(updateWrapper);

        //将当前地址的isdefault字段改为1
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 修改时，根据id获取地址，完成回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R getById(@PathVariable Long id){

        AddressBook addressBook = addressBookService.getById(id);
        if(addressBook != null){
            return R.success(addressBook);
        }
        return R.error("没有找到该对象!");
    }


    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> updateAddress(@RequestBody AddressBook addressBook){

        addressBookService.updateById(addressBook);
        return R.success("修改地址成功！");
    }


    /**
     * 根据id删除地址
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> deleteAddress( @RequestParam Long id){
        log.info("id = {}",id);
        addressBookService.removeById(id);

        return R.success("地址删除成功！");
    }


}
