package com.bean.lostandfound.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Claim {
    private Integer id;
    private Integer lostFoundId;
    private Integer claimerId;
    private String description;
    private String contactPhone;
    private Integer status; // 0=待审核, 1=已通过, 2=已拒绝, 3=已完成
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}