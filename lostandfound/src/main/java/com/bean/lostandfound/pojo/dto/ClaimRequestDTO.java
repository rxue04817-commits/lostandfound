package com.bean.lostandfound.pojo.dto;

import lombok.Data;

@Data
public class ClaimRequestDTO {
    private Integer lostFoundId;
    private String description;
    private String contactPhone;
}