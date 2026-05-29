package com.bean.lostandfound.pojo.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer role;
    private Integer status;
}
