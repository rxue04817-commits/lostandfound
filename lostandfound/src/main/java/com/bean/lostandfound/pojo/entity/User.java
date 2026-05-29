package com.bean.lostandfound.pojo.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private Integer role = 0;

    private Integer status = 1;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}