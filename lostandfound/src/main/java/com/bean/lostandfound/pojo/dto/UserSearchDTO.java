package com.bean.lostandfound.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String username;
    private String realName;
    private String phone;
    private Integer role;
    private Integer status;

    public Integer getPage() {
        return page != null ? page : 1;
    }

    public Integer getSize() {
        return size != null ? size : 10;
    }
}
