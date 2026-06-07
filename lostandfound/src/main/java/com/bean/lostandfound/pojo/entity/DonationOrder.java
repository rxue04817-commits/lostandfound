package com.bean.lostandfound.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DonationOrder {
    private String outTradeNo;
    private Integer userId;
    private Integer lostFoundId;
    private BigDecimal totalAmount;
    private String subject;
    private String tradeStatus;
    private String tradeNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}