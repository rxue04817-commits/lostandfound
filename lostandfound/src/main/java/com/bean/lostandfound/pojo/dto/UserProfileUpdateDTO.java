package com.bean.lostandfound.pojo.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 用户个人信息修改DTO
 * 用于普通用户修改自己的部分信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileUpdateDTO {
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
}
