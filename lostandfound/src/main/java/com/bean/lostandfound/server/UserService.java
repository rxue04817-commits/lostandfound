package com.bean.lostandfound.server;

import com.bean.lostandfound.pojo.dto.UserProfileUpdateDTO;
import com.bean.lostandfound.pojo.dto.UserRegisterDTO;
import com.bean.lostandfound.pojo.dto.UserSearchDTO;
import com.bean.lostandfound.pojo.entity.User;
import com.bean.lostandfound.pojo.vo.AdminUserDetailVO;
import com.bean.lostandfound.pojo.vo.UserInfoVO;
import com.bean.lostandfound.result.PageResult;

public interface UserService {

    User getById(Integer id);

    User login(String username, String password);

    void updateUserProfile(Integer currentUserId, UserProfileUpdateDTO profileUpdateDTO);

    String register(UserRegisterDTO userRegisterDTO);

    void logout(Integer userId);

    void updateUserProfile(Integer userId, String avatarUrl);

    PageResult getUserList(UserSearchDTO userSearchDTO);

    AdminUserDetailVO getUserDetail(Integer id);

    void updateUserStatus(Integer id, Integer status, Integer operatorId);

    void updateUserRole(Integer id, Integer role, Integer operatorId);
}
