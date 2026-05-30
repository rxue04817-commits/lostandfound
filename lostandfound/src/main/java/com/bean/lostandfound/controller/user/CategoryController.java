package com.bean.lostandfound.controller.user;

import com.bean.lostandfound.pojo.entity.ItemCategory;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.ItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private ItemCategoryService itemCategoryService;

    @GetMapping
    public Result<List<ItemCategory>> listCategories() {
        return Result.success(itemCategoryService.listAll());
    }
}
