package com.bean.lostandfound.pojo.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LostFoundImage {
    private Integer id;

    private Integer lostFoundId;

    private String imageUrl;

    private LocalDateTime createdAt;
}