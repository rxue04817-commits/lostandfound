package com.bean.lostandfound.pojo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DonationOrderDTO {
    private Integer lostFoundId;
    private BigDecimal amount;
}