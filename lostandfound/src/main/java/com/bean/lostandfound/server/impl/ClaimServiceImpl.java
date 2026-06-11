package com.bean.lostandfound.server.impl;

import com.bean.lostandfound.utils.BaseContext;
import com.bean.lostandfound.exception.BaseException;
import com.bean.lostandfound.exception.NotFoundException;
import com.bean.lostandfound.mapper.ClaimMapper;
import com.bean.lostandfound.mapper.LostFoundMapper;
import com.bean.lostandfound.mapper.UserMapper;
import com.bean.lostandfound.pojo.dto.ClaimRejectDTO;
import com.bean.lostandfound.pojo.dto.ClaimRequestDTO;
import com.bean.lostandfound.pojo.entity.Claim;
import com.bean.lostandfound.pojo.entity.LostFound;
import com.bean.lostandfound.pojo.entity.User;
import com.bean.lostandfound.pojo.vo.ClaimVO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.server.ClaimService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimServiceImpl implements ClaimService {

    @Autowired
    private ClaimMapper claimMapper;
    
    @Autowired
    private LostFoundMapper lostFoundMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void submitClaim(ClaimRequestDTO requestDTO) {
        Long currentId = BaseContext.getCurrentId();

        Integer userId = currentId != null ? currentId.intValue() : null;
        System.out.println("userID"+userId);
        LostFound lf = lostFoundMapper.getById(requestDTO.getLostFoundId());
        if (lf == null) {
            throw new NotFoundException("信息不存在");
        }
        if (lf.getItemType() != 1) {
            throw new BaseException("只有拾物信息可以被认领");
        }
        if (lf.getStatus() != 1) {
            throw new BaseException("该拾物当前状态不可认领");
        }
        if (lf.getUserId().equals(userId)) {
            throw new BaseException("不能认领自己发布的拾物");
        }
        
        Claim pendingClaim = claimMapper.findPendingByLostFoundIdAndClaimerId(requestDTO.getLostFoundId(), userId);
        if (pendingClaim != null) {
            throw new BaseException("您已经有一条待审核的认领申请");
        }

        List<Claim> activeClaims = claimMapper.findActiveByLostFoundId(requestDTO.getLostFoundId());
        for (Claim c : activeClaims) {
            if (c.getStatus() == 3) {
                throw new BaseException("该物品已被成功认领，无法再次申请");
            }
        }

        Claim claim = new Claim();
        BeanUtils.copyProperties(requestDTO, claim);
        claim.setClaimerId(userId);
        claim.setStatus(0); // 待审核
        System.out.println( claim );
        claimMapper.insert(claim);
    }

    @Override
    public PageResult getMyClaims(int page, int pageSize) {
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        PageHelper.startPage(page, pageSize);
        List<Claim> claims = claimMapper.findByClaimerId(userId);
        Page<Claim> p = (Page<Claim>) claims;
        
        List<ClaimVO> vos = claims.stream().map(this::convertToVO).collect(Collectors.toList());
        return new PageResult(p.getTotal(), vos);
    }

    @Override
    public PageResult getReceivedClaims(int page, int pageSize) {
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        PageHelper.startPage(page, pageSize);
        List<Claim> claims = claimMapper.findByLostFoundOwnerId(userId);
        Page<Claim> p = (Page<Claim>) claims;
        
        List<ClaimVO> vos = claims.stream().map(this::convertToVO).collect(Collectors.toList());
        return new PageResult(p.getTotal(), vos);
    }

    @Override
    public ClaimVO getClaimById(Integer id) {
        Claim claim = claimMapper.findById(id);
        if (claim == null) {
            throw new NotFoundException("认领记录不存在");
        }
        
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        LostFound lf = lostFoundMapper.getById(claim.getLostFoundId());
        
        if (!claim.getClaimerId().equals(userId) && !lf.getUserId().equals(userId)) {
            throw new BaseException("无权查看该认领记录");
        }
        
        return convertToVO(claim);
    }

    @Override
    @Transactional
    public void approveClaim(Integer id) {
        Claim claim = claimMapper.findById(id);
        if (claim == null) {
            throw new NotFoundException("认领记录不存在");
        }
        if (claim.getStatus() != 0) {
            throw new BaseException("当前状态不可通过");
        }
        
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        LostFound lf = lostFoundMapper.getById(claim.getLostFoundId());
        if (!lf.getUserId().equals(userId)) {
            throw new BaseException("无权操作：只能处理自己发布的拾物认领");
        }
        
        claimMapper.updateStatus(id, 1, null); // 已通过
        
        claimMapper.rejectOtherPendingClaims(claim.getLostFoundId());
    }

    @Override
    public void rejectClaim(Integer id, ClaimRejectDTO rejectDTO) {
        Claim claim = claimMapper.findById(id);
        if (claim == null) {
            throw new NotFoundException("认领记录不存在");
        }
        if (claim.getStatus() != 0) {
            throw new BaseException("当前状态不可拒绝");
        }
        
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        LostFound lf = lostFoundMapper.getById(claim.getLostFoundId());
        if (!lf.getUserId().equals(userId)) {
            throw new BaseException("无权操作：只能处理自己发布的拾物认领");
        }
        
        claimMapper.updateStatus(id, 2, rejectDTO.getRejectReason()); // 已拒绝
    }

    @Override
    @Transactional
    public void completeClaim(Integer id) {
        Claim claim = claimMapper.findById(id);
        if (claim == null) {
            throw new NotFoundException("认领记录不存在");
        }
        if (claim.getStatus() != 1) {
            throw new BaseException("只有已通过的认领可以确认归还");
        }
        
        Long currentId = BaseContext.getCurrentId();
        Integer userId = currentId != null ? currentId.intValue() : null;
        LostFound lf = lostFoundMapper.getById(claim.getLostFoundId());
        if (!lf.getUserId().equals(userId)) {
            throw new BaseException("无权操作");
        }
        
        // 更新认领状态为已完成
        claimMapper.updateStatus(id, 3, null);
        
        // 同步更新失物/拾物状态为已归还（status=2）
        LostFound updateLf = new LostFound();
        updateLf.setId(claim.getLostFoundId());
        updateLf.setStatus(2); // 2=已找回/已归还
        updateLf.setUpdatedAt(java.time.LocalDateTime.now()); // 设置更新时间
        lostFoundMapper.updateStatus(updateLf);
        
        System.out.println("认领完成，物品ID: " + claim.getLostFoundId() + " 状态已更新为: 2 (已归还)");
    }
    
    private ClaimVO convertToVO(Claim claim) {
        ClaimVO vo = new ClaimVO();
        BeanUtils.copyProperties(claim, vo);
        
        LostFound lf = lostFoundMapper.getById(claim.getLostFoundId());
        if (lf != null) {
            vo.setLostFoundTitle(lf.getTitle());
        }
        
        User claimer = userMapper.getById(claim.getClaimerId());
        if (claimer != null) {
            vo.setClaimerName(claimer.getUsername());
        }
        
        return vo;
    }
}