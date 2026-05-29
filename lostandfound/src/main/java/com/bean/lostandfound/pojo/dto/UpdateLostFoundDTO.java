package com.bean.lostandfound.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLostFoundDTO {
    private String title;
    private String content;
    private String location;
    private String date;
    private String contactPhone;
    private List<String> imageUrls;
}
