package com.bean.lostandfound.mapper;

import com.bean.lostandfound.pojo.entity.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentMapper {


    public List<Comment> getByLostFoundId(Integer id);
    @Insert("insert into comment(lost_found_id,user_id,content,created_at) values(#{lostFoundId},#{userId},#{content},#{now})")
    void addComment(Integer lostFoundId, Integer userId, String content, LocalDateTime now);
    @Select("select * from comment where id=#{id}")
    Comment findById(Integer id);
    @Delete("delete from comment where id=#{id}")
    void deleteBYId(Integer id);
    @Delete("delete from comment where lost_found_id=#{id}")
    void deleteByLostId(Integer id);
}
