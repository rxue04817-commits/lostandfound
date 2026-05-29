package com.bean.lostandfound.server;

import com.bean.lostandfound.pojo.dto.UserProfileUpdateDTO;
import com.bean.lostandfound.pojo.dto.UserRegisterDTO;
import com.bean.lostandfound.pojo.entity.User;

public interface UserService {


    User getById(Integer id);

    User login(String username, String password);

    void updateUserProfile(Integer currentUserId, UserProfileUpdateDTO profileUpdateDTO);

    String register(UserRegisterDTO userRegisterDTO);

    void logout(Integer userId);
    // 在 UserService 接口中添加新方法声明
    public void updateUserProfile(Integer userId, String avatarUrl);


}
