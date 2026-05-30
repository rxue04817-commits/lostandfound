package com.bean.lostandfound.mapper;

import com.bean.lostandfound.pojo.entity.ItemCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ItemCategoryMapper {

    @Select("select * from item_category where status = 1 order by sort_order, id")
    List<ItemCategory> listAll();

    @Select("select * from item_category where id = #{id}")
    ItemCategory getById(Integer id);
}
