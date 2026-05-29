package com.bean.lostandfound.pojo.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseVO {
    private String token;
    private UserInfoVO userInfoVO;

}
