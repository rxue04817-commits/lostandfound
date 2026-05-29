package com.bean.lostandfound.pojo.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private Integer id;

    private Integer userId;

    private String title;

    private String content;

    private Integer type;

    private Integer isRead = 0;

    private LocalDateTime createdAt;
}