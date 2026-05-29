package com.bean.lostandfound.pojo.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Integer id;

    private Integer senderId;

    private Integer receiverId;

    private String content;

    private Integer isRead = 0;

    private LocalDateTime createdAt;
}