package com.bean.lostandfound.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data

@AllArgsConstructor
@NoArgsConstructor
public class ClaimVO {
    private Integer id;
    private Integer lostFoundId;
    private String lostFoundTitle;
    private Integer claimerId;
    private String claimerName;
    private String description;
    private String contactPhone;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createdAt;
}