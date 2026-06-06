package com.bean.lostandfound.controller.user;

import com.bean.lostandfound.pojo.dto.ClaimRejectDTO;
import com.bean.lostandfound.pojo.dto.ClaimRequestDTO;
import com.bean.lostandfound.pojo.vo.ClaimVO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.ClaimService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/claims")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PostMapping
    public Result submitClaim(@RequestBody ClaimRequestDTO requestDTO) {
        claimService.submitClaim(requestDTO);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<PageResult> getMyClaims(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(claimService.getMyClaims(page, pageSize));
    }

    @GetMapping("/received")
    public Result<PageResult> getReceivedClaims(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(claimService.getReceivedClaims(page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<ClaimVO> getClaimById(@PathVariable Integer id) {
        return Result.success(claimService.getClaimById(id));
    }

    @PutMapping("/{id}/approve")
    public Result approveClaim(@PathVariable Integer id) {
        claimService.approveClaim(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    public Result rejectClaim(@PathVariable Integer id, @RequestBody ClaimRejectDTO rejectDTO) {
        claimService.rejectClaim(id, rejectDTO);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    public Result completeClaim(@PathVariable Integer id) {
        claimService.completeClaim(id);
        return Result.success();
    }
}