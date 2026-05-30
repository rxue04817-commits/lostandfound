package com.bean.lostandfound.server.impl;

import com.bean.lostandfound.exception.NotFoundException;
import com.bean.lostandfound.exception.UnauthorizedException;
import com.bean.lostandfound.mapper.CommentMapper;
import com.bean.lostandfound.mapper.LostFoundImageMapper;
import com.bean.lostandfound.mapper.LostFoundMapper;
import com.bean.lostandfound.mapper.UserMapper;
import com.bean.lostandfound.pojo.dto.LostFoundDTO;
import com.bean.lostandfound.pojo.dto.LostSearchDTO;
import com.bean.lostandfound.pojo.dto.UpdateLostFoundDTO;
import com.bean.lostandfound.pojo.entity.Comment;
import com.bean.lostandfound.pojo.entity.LostFound;
import com.bean.lostandfound.pojo.entity.LostFoundImage;
import com.bean.lostandfound.pojo.entity.User;
import com.bean.lostandfound.pojo.vo.CommentVO;
import com.bean.lostandfound.pojo.vo.LostFoundDetailVO;
import com.bean.lostandfound.pojo.vo.LostFoundVO;
import com.bean.lostandfound.pojo.vo.UserInfoVO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.server.LostService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LostServiceImpl implements LostService {
    @Autowired
    private LostFoundMapper lostFoundMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LostFoundImageMapper lostFoundImageMapper;
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public PageResult getLostFoundList(LostSearchDTO lostSearchDTO) {


        // 开启分页
        PageHelper.startPage(lostSearchDTO.getPage(), lostSearchDTO.getSize());

        // 查询失物招领列表
        List<LostFound> lostFounds = lostFoundMapper.selectByCondition(lostSearchDTO);

        // 转换为 VO 对象
        List<LostFoundVO> voList = lostFounds.stream().map(this::convertToVO).collect(Collectors.toList());

        // 封装分页结果
        Page<LostFound> page = (Page<LostFound>) lostFounds;
        return new PageResult(page.getTotal(), voList);
    }

    private LostFoundVO convertToVO(LostFound lostFound) {  // 修正参数类型
        LostFoundVO vo = new LostFoundVO();
        BeanUtils.copyProperties(lostFound, vo);

        // 获取用户信息 (通过 userId)
        User user = userMapper.getById(lostFound.getUserId());
        vo.setUserInfo(user);

        // 获取图片链接 (通过 lostFoundId)
        List<LostFoundImage> images = lostFoundImageMapper.selectByLostFoundId(lostFound.getId());
        List<String> imageUrls = images.stream()
                .map(LostFoundImage::getImageUrl)
                .collect(Collectors.toList());
        vo.setImages(imageUrls);

        return vo;
    }
    public LostFoundDetailVO getLostFoundDetail(Integer id) {
        // 查询失物信息
        LostFound lostFound = lostFoundMapper.getById(id);
        if (lostFound == null) {
            throw new RuntimeException("失物信息不存在");
        }

        // 组装返回数据
        LostFoundDetailVO detailVO = new LostFoundDetailVO();
        BeanUtils.copyProperties(lostFound, detailVO);

        // 查询用户信息
        User user = userMapper.getById(lostFound.getUserId());
        if (user != null) {
            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(user, userInfoVO);
            detailVO.setUserInfo(userInfoVO);
        }

        // 查询图片列表
        List<LostFoundImage> images = lostFoundImageMapper.selectByLostFoundId(id);
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = images.stream()
                    .map(LostFoundImage::getImageUrl)
                    .collect(Collectors.toList());
            detailVO.setImages(imageUrls);
        } else {
            detailVO.setImages(new ArrayList<>());
        }

        // 查询评论列表
        List<Comment> comments = commentMapper.getByLostFoundId(id);
        List<CommentVO> commentVOs = new ArrayList<>();
        if (comments != null && !comments.isEmpty()) {
            commentVOs = comments.stream().map(this::convertCommentToVO).collect(Collectors.toList());
        }
        detailVO.setComments(commentVOs);
        return detailVO;
    }
    private CommentVO convertCommentToVO(Comment comment) {
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        // 获取评论用户信息
        User commentUser = userMapper.getById(comment.getUserId());
        if (commentUser != null) {
            UserInfoVO commentUserInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(commentUser, commentUserInfoVO);
            commentVO.setUserInfo(commentUserInfoVO);
        }

        return commentVO;
    }
    //发布失物招领
    public void publishLostFound(LostFoundDTO lostFoundDTO, Integer userId) {
        // 1. 创建失物信息实体
        LostFound lostFound = new LostFound();
        BeanUtils.copyProperties(lostFoundDTO, lostFound);
        lostFound.setUserId(userId);
        lostFound.setCreatedAt(LocalDateTime.now());
        lostFound.setStatus(0); // 默认状态为1（发布中）

        // 2. 保存失物信息
        lostFoundMapper.insert(lostFound);

        // 3. 保存图片信息
        if (lostFoundDTO.getImageUrls() != null && !lostFoundDTO.getImageUrls().isEmpty()) {
            for (String imageUrl : lostFoundDTO.getImageUrls()) {
                LostFoundImage image = new LostFoundImage();
                image.setLostFoundId(lostFound.getId());
                image.setImageUrl(imageUrl);
                image.setCreatedAt(LocalDateTime.now());
                lostFoundImageMapper.insert(image);
            }
        }
    }
    //发布者修改信息
    public void updateLostFound(Integer id, UpdateLostFoundDTO updateLostFoundDTO, Integer currentUserId) {
        // 1. 查询原始失物信息
        LostFound originalLostFound = lostFoundMapper.getById(id);
        if (originalLostFound == null) {
            throw new NotFoundException("失物信息不存在");
        }

        // 2. 验证权限（检查是否为发布者）
        if (!originalLostFound.getUserId().equals(currentUserId)) {
            throw new UnauthorizedException("无权限操作该失物信息");
        }

        // 3. 更新失物信息
        BeanUtils.copyProperties(updateLostFoundDTO, originalLostFound);
        originalLostFound.setUpdatedAt(LocalDateTime.now());

        // 执行更新操作
        lostFoundMapper.update(originalLostFound);

        // 4. 删除原有的图片信息
        lostFoundImageMapper.deleteByLostFoundId(id);

        // 5. 插入新的图片信息
        if (updateLostFoundDTO.getImageUrls() != null && !updateLostFoundDTO.getImageUrls().isEmpty()) {
            for (String imageUrl : updateLostFoundDTO.getImageUrls()) {
                LostFoundImage image = new LostFoundImage();
                image.setLostFoundId(id);
                image.setImageUrl(imageUrl);
                image.setCreatedAt(LocalDateTime.now());
                lostFoundImageMapper.insert(image);
            }
        }
    }
    public void updateLostFoundStatus(Integer id, Integer status, Integer userId, Integer userRole) {
        // 1. 查询原始失物信息
        LostFound lostFound = lostFoundMapper.getById(id);
        if (lostFound == null) {
            throw new NotFoundException("失物信息不存在");
        }

        // 2. 权限和状态验证
        validateStatusUpdate(lostFound, status, userId, userRole);

        // 3. 更新状态
        lostFound.setStatus(status);
        lostFound.setUpdatedAt(LocalDateTime.now());
        lostFoundMapper.updateStatus(lostFound);
    }
    /**
     * 验证状态更新权限
     * @param lostFound 失物信息
     * @param newStatus 新状态
     * @param userId 当前用户ID
     * @param userRole 当前用户角色
     */
    private void validateStatusUpdate(LostFound lostFound, Integer newStatus, Integer userId, Integer userRole) {
        // 管理员权限验证
        if (userRole == 1) { // 管理员
            // 管理员可以将状态设置为 0(待审核)、1(已审核)、3(已过期)
            if (newStatus != 0 && newStatus != 1 && newStatus != 3) {
                throw new IllegalStateException("管理员只能将状态设置为待审核、已审核或已过期");
            }
            return;
        }

        // 普通用户权限验证
        if (userRole == 0) { // 普通用户
            // 普通用户只能更新自己发布的失物信息
            if (!lostFound.getUserId().equals(userId)) {
                throw new UnauthorizedException("无权限操作他人发布的失物信息");
            }

            // 普通用户只能将状态设置为 2(已领取)
            if (newStatus != 2) {
                throw new UnauthorizedException("普通用户只能将状态设置为已领取");
            }

            // 普通用户只能在已审核状态下设置为已领取
            if (lostFound.getStatus() != 1) {
                throw new IllegalStateException("只能在已审核状态下设置为已领取");
            }

            return;
        }

        throw new UnauthorizedException("无权限操作");
    }
    public void deleteById(Integer id, Integer userId, Integer userRole) {
        LostFound lostFound = lostFoundMapper.getById(id);
        if (lostFound == null) {
            throw new NotFoundException("失物信息不存在");
        }
        if (userRole != 1 && !lostFound.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权限删除他人发布的信息");
        }
        commentMapper.deleteByLostId(id);
        lostFoundImageMapper.deleteByLostFoundId(id);
        lostFoundMapper.deleteById(id);
    }
}
