package com.bean.lostandfound.server.impl;

import com.bean.lostandfound.mapper.ItemCategoryMapper;
import com.bean.lostandfound.pojo.entity.ItemCategory;
import com.bean.lostandfound.server.ItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemCategoryServiceImpl implements ItemCategoryService {

    @Autowired
    private ItemCategoryMapper itemCategoryMapper;

    @Override
    public List<ItemCategory> listAll() {
        return itemCategoryMapper.listAll();
    }
}
