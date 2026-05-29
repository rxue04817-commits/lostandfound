package com.bean.lostandfound.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    private Integer id;

    private Integer lostFoundId;

    private Integer userId;

    private String content;

    private LocalDateTime createdAt;
}