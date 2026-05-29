package com.bean.lostandfound.server;

import com.bean.lostandfound.pojo.entity.Comment;
import com.bean.lostandfound.pojo.vo.CommentVO;

import java.util.List;

public interface CommentService {
    List<CommentVO> getById(Integer id);

    void addComment(Integer lostFoundId,Integer userId,String content);

    Comment findById(Integer id);

    void deleteById(Integer id);
}
