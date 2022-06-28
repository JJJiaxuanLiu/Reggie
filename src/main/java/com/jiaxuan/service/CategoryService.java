package com.jiaxuan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaxuan.domain.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
