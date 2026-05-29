package com.bean.lostandfound.pojo.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 用户注册DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO {
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer role = 0; // 默认为普通用户
}
