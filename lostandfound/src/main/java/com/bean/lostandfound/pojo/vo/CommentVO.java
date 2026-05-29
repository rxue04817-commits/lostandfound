package com.bean.lostandfound.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 评论数据结构
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {
    private Integer id;
    private Integer lostFoundId;
    private Integer userId;
    private String content;
    private LocalDateTime createdAt;
    private UserInfoVO userInfo;

    // getters and setters
}
