package com.bean.lostandfound.controller.user;

import com.bean.lostandfound.pojo.entity.Comment;
import com.bean.lostandfound.pojo.vo.CommentVO;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.CommentService;
import com.bean.lostandfound.server.CommentService;
import com.bean.lostandfound.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    CommentService commentService;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id){
        List<CommentVO> commentVOList = commentService.getById(id);
        return Result.success(commentVOList);
    }
    @PostMapping
    public Result addComment(Integer lostFoundId,HttpServletRequest request,@RequestBody Map<String, Object> requestBody){
        String token=request.getHeader(jwtUtil.getHeader());
        if (token == null ) {
            return Result.error("未授权");
        }

        if (!jwtUtil.validateToken(token)) {
            return Result.error("token无效或已过期");
        }
        Integer userId=jwtUtil.getUserIdFromToken(token);
        String content= (String) requestBody.get("content");
        commentService.addComment(lostFoundId,userId,content);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable Integer id, HttpServletRequest request) {
        // 从token中解析用户信息
        String token=request.getHeader(jwtUtil.getHeader());
        if (token == null ) {
            return Result.error("未授权");
        }

        if (!jwtUtil.validateToken(token)) {
            return Result.error("token无效或已过期");
        }
        Integer userId=jwtUtil.getUserIdFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        // 查询要删除的评论
        Comment comment = commentService.findById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        // 权限检查
        if (comment.getUserId()==userId ||role==1) {
            commentService.deleteById(id);
        }else {
            return Result.error("权限不足");
        }

        return Result.success("删除成功");
    }

}
