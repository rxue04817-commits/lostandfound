package com.bean.lostandfound.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCategory {
    private Integer id;
    private String name;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
}
