package com.bean.lostandfound.server.impl;

import com.bean.lostandfound.mapper.CommentMapper;
import com.bean.lostandfound.mapper.UserMapper;
import com.bean.lostandfound.pojo.entity.Comment;
import com.bean.lostandfound.pojo.entity.User;
import com.bean.lostandfound.pojo.vo.CommentVO;
import com.bean.lostandfound.pojo.vo.UserInfoVO;
import com.bean.lostandfound.server.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    CommentMapper commentMapper;
    @Autowired
    UserMapper userMapper;
    public List<CommentVO> getById(Integer id) {
        List<CommentVO> commentVOList = new ArrayList<>();
        List<Comment> commentList = commentMapper.getByLostFoundId(id);

        for (Comment comment : commentList) {
            CommentVO commentVO = new CommentVO();

            // 复制评论基本属性
            BeanUtils.copyProperties(comment, commentVO);

            // 获取用户信息
            Integer userId = comment.getUserId();
            User user = userMapper.getById(userId);

            // 创建并填充UserInfoVO
            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(user,userInfoVO );

            // 设置userInfo到commentVO
            commentVO.setUserInfo(userInfoVO);

            commentVOList.add(commentVO);
        }

        return commentVOList;
    }

    @Override
    public void addComment(Integer lostFoundId, Integer userId, String content) {
        LocalDateTime now = LocalDateTime.now();
        commentMapper.addComment(lostFoundId,userId,content,now);

    }
    public Comment findById(Integer id){
        return commentMapper.findById(id);
    }
    public void deleteById(Integer id){
        commentMapper.deleteBYId(id);
    }
}
