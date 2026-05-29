package com.bean.lostandfound.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 用户信息数据结构
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {
    private Integer id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;

}
