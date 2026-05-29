package com.bean.lostandfound.mapper;

import com.bean.lostandfound.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where id=#{id}")
    User getById(Integer id);
    @Select("select * from user where username=#{username}")
    User getByUsername(String username);

    void updateById(User user);

    void insert(User user);
}
