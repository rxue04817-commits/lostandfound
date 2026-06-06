package com.bean.lostandfound.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
// 失物详情数据结构
public class LostFoundDetailVO {
    private Integer id;
    private Integer userId;
    private Integer itemType;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private String content;
    private String location;
    private LocalDate date;
    private Integer status;
    private String contactPhone;
    private LocalDateTime createdAt;
    private UserInfoVO userInfo;
    private List<String> images;
    private List<CommentVO> comments;
    
    // 对拾物额外返回
    private Integer claimStatus; // 当前用户的认领状态
    private Integer pendingClaimCount; // 待处理认领数量（仅发布者可见）
}

