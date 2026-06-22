package com.tourism.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tourism.entity.Category;
import com.tourism.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {
}
