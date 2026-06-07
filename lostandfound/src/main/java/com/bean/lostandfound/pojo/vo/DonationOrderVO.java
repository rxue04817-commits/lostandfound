package com.bean.lostandfound.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DonationOrderVO {
    private String outTradeNo;
    private Integer userId;
    private String username;
    private Integer lostFoundId;
    private String lostFoundTitle;
    private BigDecimal totalAmount;
    private String subject;
    private String tradeStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}