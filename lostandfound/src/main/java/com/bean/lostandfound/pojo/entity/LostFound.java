package com.bean.lostandfound.pojo.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LostFound {
    private Integer id;

    private Integer userId;

    private String title;

    private String content;

    private String location;

    private LocalDate date;

    private Integer status = 0;

    private String contactPhone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}