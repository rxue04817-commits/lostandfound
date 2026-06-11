package com.bean.lostandfound.server.impl;

import com.bean.lostandfound.context.BaseContext;
import com.bean.lostandfound.exception.BaseException;
import com.bean.lostandfound.exception.NotFoundException;
import com.bean.lostandfound.exception.UnauthorizedException;
import com.bean.lostandfound.mapper.*;
import com.bean.lostandfound.pojo.dto.LostFoundDTO;
import com.bean.lostandfound.pojo.dto.LostSearchDTO;
import com.bean.lostandfound.pojo.dto.UpdateLostFoundDTO;
import com.bean.lostandfound.pojo.entity.Comment;
import com.bean.lostandfound.pojo.entity.ItemCategory;
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

    @Autowired
    private ItemCategoryMapper itemCategoryMapper;

    @Autowired
    private ClaimMapper claimMapper;

    @Override
    public PageResult getLostFoundList(LostSearchDTO lostSearchDTO) {
        PageHelper.startPage(lostSearchDTO.getPage(), lostSearchDTO.getSize());
        List<LostFound> lostFounds = lostFoundMapper.selectByCondition(lostSearchDTO);
        List<LostFoundVO> voList = lostFounds.stream().map(this::convertToVO).collect(Collectors.toList());
        Page<LostFound> page = (Page<LostFound>) lostFounds;
        return new PageResult(page.getTotal(), voList);
    }

    private LostFoundVO convertToVO(LostFound lostFound) {
        LostFoundVO vo = new LostFoundVO();
        BeanUtils.copyProperties(lostFound, vo);

        User user = userMapper.getById(lostFound.getUserId());
        vo.setUserInfo(user);

        ItemCategory category = itemCategoryMapper.getById(lostFound.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        List<LostFoundImage> images = lostFoundImageMapper.selectByLostFoundId(lostFound.getId());
        List<String> imageUrls = images.stream()
                .map(LostFoundImage::getImageUrl)
                .collect(Collectors.toList());
        vo.setImages(imageUrls);

        return vo;
    }

    public LostFoundDetailVO getLostFoundDetail(Integer id) {
        LostFound lostFound = lostFoundMapper.getById(id);
        if (lostFound == null) {
            throw new NotFoundException("信息不存在");
        }

        LostFoundDetailVO detailVO = new LostFoundDetailVO();
        BeanUtils.copyProperties(lostFound, detailVO);

        ItemCategory category = itemCategoryMapper.getById(lostFound.getCategoryId());
        if (category != null) {
            detailVO.setCategoryName(category.getName());
        }

        User user = userMapper.getById(lostFound.getUserId());
        if (user != null) {
            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(user, userInfoVO);
            detailVO.setUserInfo(userInfoVO);
        }

        List<LostFoundImage> images = lostFoundImageMapper.selectByLostFoundId(id);
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = images.stream()
                    .map(LostFoundImage::getImageUrl)
                    .collect(Collectors.toList());
            detailVO.setImages(imageUrls);
        } else {
            detailVO.setImages(new ArrayList<>());
        }

        List<Comment> comments = commentMapper.getByLostFoundId(id);
        List<CommentVO> commentVOs = new ArrayList<>();
        if (comments != null && !comments.isEmpty()) {
            commentVOs = comments.stream().map(this::convertCommentToVO).collect(Collectors.toList());
        }
        detailVO.setComments(commentVOs);

        if (lostFound.getItemType() != null && lostFound.getItemType() == 1) {
            try {
                Long currentId = com.bean.lostandfound.utils.BaseContext.getCurrentId();
                Integer currentUserId = currentId != null ? currentId.intValue() : null;
                if (currentUserId != null) {
                    if (lostFound.getUserId().equals(currentUserId)) {
                        int pendingCount = claimMapper.countPendingByLostFoundId(id);
                        detailVO.setPendingClaimCount(pendingCount);
                    } else {
                        com.bean.lostandfound.pojo.entity.Claim claim = claimMapper.findLatestByLostFoundIdAndClaimerId(id, currentUserId);
                        if (claim != null) {
                            detailVO.setClaimStatus(claim.getStatus());
                        }
                    }
                }
            } catch (Exception e) {
                // If not logged in or other issues, ignore
            }
        }

        return detailVO;
    }

    private CommentVO convertCommentToVO(Comment comment) {
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        User commentUser = userMapper.getById(comment.getUserId());
        if (commentUser != null) {
            UserInfoVO commentUserInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(commentUser, commentUserInfoVO);
            commentVO.setUserInfo(commentUserInfoVO);
        }

        return commentVO;
    }

    public void publishLostFound(LostFoundDTO lostFoundDTO, Integer userId) {
        validatePublishFields(lostFoundDTO);

        LostFound lostFound = new LostFound();
        BeanUtils.copyProperties(lostFoundDTO, lostFound);
        lostFound.setUserId(userId);
        lostFound.setCreatedAt(LocalDateTime.now());
        lostFound.setStatus(0);

        lostFoundMapper.insert(lostFound);

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

    private void validatePublishFields(LostFoundDTO dto) {
        if (dto.getItemType() == null || (dto.getItemType() != 0 && dto.getItemType() != 1)) {
            throw new BaseException("请选择信息类型（失物或拾物）");
        }
        if (dto.getCategoryId() == null) {
            throw new BaseException("请选择物品分类");
        }
        ItemCategory category = itemCategoryMapper.getById(dto.getCategoryId());
        if (category == null) {
            throw new BaseException("物品分类不存在");
        }
    }

    public void updateLostFound(Integer id, UpdateLostFoundDTO updateLostFoundDTO, Integer currentUserId) {
        LostFound originalLostFound = lostFoundMapper.getById(id);
        if (originalLostFound == null) {
            throw new NotFoundException("信息不存在");
        }

        if (!originalLostFound.getUserId().equals(currentUserId)) {
            throw new UnauthorizedException("无权限操作该信息");
        }

        if (originalLostFound.getStatus() == 2 || originalLostFound.getStatus() == 3) {
            throw new IllegalStateException("已完结或已过期的信息不可编辑");
        }

        BeanUtils.copyProperties(updateLostFoundDTO, originalLostFound);
        originalLostFound.setUpdatedAt(LocalDateTime.now());
        originalLostFound.setStatus(0);//重新审核
        lostFoundMapper.update(originalLostFound);

        lostFoundImageMapper.deleteByLostFoundId(id);

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
        LostFound lostFound = lostFoundMapper.getById(id);
        if (lostFound == null) {
            throw new NotFoundException("信息不存在");
        }

        validateStatusUpdate(lostFound, status, userId, userRole);

        lostFound.setStatus(status);
        lostFound.setUpdatedAt(LocalDateTime.now());
        lostFoundMapper.updateStatus(lostFound);
    }

    private void validateStatusUpdate(LostFound lostFound, Integer newStatus, Integer userId, Integer userRole) {
        if (userRole >= 1) {
            if (newStatus != 0 && newStatus != 1 && newStatus != 3) {
                throw new IllegalStateException("管理员只能将状态设置为待审核、已审核或已过期");
            }
            return;
        }

        if (userRole == 0) {
            if (!lostFound.getUserId().equals(userId)) {
                throw new UnauthorizedException("无权限操作他人发布的信息");
            }

            if (newStatus != 2) {
                throw new UnauthorizedException("普通用户只能将状态设置为已找回/已归还");
            }

            if (lostFound.getStatus() != 1) {
                throw new IllegalStateException("只能在已审核状态下进行此操作");
            }

            if (lostFound.getItemType() != null && lostFound.getItemType() == 1) {
                throw new UnauthorizedException("拾物信息需通过认领流程完成归还");
            }

            return;
        }

        throw new UnauthorizedException("无权限操作");
    }

    public void deleteById(Integer id, Integer userId, Integer userRole) {
        LostFound lostFound = lostFoundMapper.getById(id);
        if (lostFound == null) {
            throw new NotFoundException("信息不存在");
        }
        if (userRole != 1 && !lostFound.getUserId().equals(userId)) {
            throw new UnauthorizedException("无权限删除他人发布的信息");
        }
        commentMapper.deleteByLostId(id);
        lostFoundImageMapper.deleteByLostFoundId(id);
        lostFoundMapper.deleteById(id);
    }
}
