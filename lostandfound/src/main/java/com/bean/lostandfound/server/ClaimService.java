package com.bean.lostandfound.server;

import com.bean.lostandfound.pojo.dto.ClaimRequestDTO;
import com.bean.lostandfound.pojo.dto.ClaimRejectDTO;
import com.bean.lostandfound.pojo.vo.ClaimVO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.result.Result;
import jakarta.servlet.http.HttpServletRequest;

public interface ClaimService {
    void submitClaim(ClaimRequestDTO requestDTO);
    PageResult getMyClaims(int page, int pageSize);
    PageResult getReceivedClaims(int page, int pageSize);
    ClaimVO getClaimById(Integer id);
    void approveClaim(Integer id);
    void rejectClaim(Integer id, ClaimRejectDTO rejectDTO);
    void completeClaim(Integer id);
}