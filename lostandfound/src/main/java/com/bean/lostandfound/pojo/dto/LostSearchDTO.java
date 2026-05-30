package com.bean.lostandfound.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LostSearchDTO {
    private Integer page = 1;        // 页码，默认第一页
    private Integer size = 10;       // 每页大小，默认10条
    private String title;             //标题
    private String content;             //描述
    private String location;         // 地点筛选
    private Integer status;          // 状态筛选
    private Integer userId;
    private Integer itemType;        // 0=失物, 1=拾物
    private Integer categoryId;
    // 添加自定义getter确保返回默认值
    public Integer getPage() {
        return page != null ? page : 1;
    }

    public Integer getSize() {
        return size != null ? size : 10;
    }
}
