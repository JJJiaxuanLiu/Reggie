package com.jiaxuan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiaxuan.domain.Category;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}