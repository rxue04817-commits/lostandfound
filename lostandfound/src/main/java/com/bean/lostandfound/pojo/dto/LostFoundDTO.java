package com.bean.lostandfound.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LostFoundDTO {
    private Integer itemType;
    private Integer categoryId;
    private String title;
    private String content;
    private String location;
    private LocalDate date;
    private String contactPhone;
    private List<String> imageUrls;
}

