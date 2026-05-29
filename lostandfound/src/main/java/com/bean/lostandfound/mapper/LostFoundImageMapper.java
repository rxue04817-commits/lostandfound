package com.bean.lostandfound.mapper;

import com.bean.lostandfound.pojo.entity.LostFoundImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LostFoundImageMapper {

    List<LostFoundImage> selectByLostFoundId(Integer id);

    void insert(LostFoundImage image);
    @Delete("delete from lost_found_image where lost_found_id=#{id}")
    void deleteByLostFoundId(Integer id);
}
