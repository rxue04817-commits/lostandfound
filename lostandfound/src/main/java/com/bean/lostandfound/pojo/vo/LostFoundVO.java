// LostFoundVO.java
package com.bean.lostandfound.pojo.vo;

import com.bean.lostandfound.pojo.entity.LostFound;
import com.bean.lostandfound.pojo.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class LostFoundVO extends LostFound {
    private User userInfo;
    private List<String> images;

    // 从 LostFound 继承的属性:
    // id, userId, title, content, location, date, status, contactPhone, createdAt, updatedAt
}
