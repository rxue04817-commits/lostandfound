package com.bean.lostandfound.controller.admin;

import com.bean.lostandfound.exception.UnauthorizedException;
import com.bean.lostandfound.pojo.dto.UserSearchDTO;
import com.bean.lostandfound.pojo.vo.AdminUserDetailVO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.UserService;
import com.bean.lostandfound.utils.AuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthHelper authHelper;

    @GetMapping
    public Result<PageResult> getUserList(UserSearchDTO userSearchDTO, HttpServletRequest request) {
        try {
            authHelper.requireAdmin(request);
            return Result.success(userService.getUserList(userSearchDTO));
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<AdminUserDetailVO> getUserDetail(@PathVariable Integer id, HttpServletRequest request) {
        try {
            authHelper.requireAdmin(request);
            return Result.success(userService.getUserDetail(id));
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Integer id,
                                           @RequestBody Map<String, Integer> body,
                                           HttpServletRequest request) {
        try {
            authHelper.requireAdmin(request);
            Integer operatorId = authHelper.getUserId(request);
            Integer status = body.get("status");
            userService.updateUserStatus(id, status, operatorId);
            return Result.success("状态更新成功");
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/role")
    public Result<String> updateUserRole(@PathVariable Integer id,
                                         @RequestBody Map<String, Integer> body,
                                         HttpServletRequest request) {
        try {
            authHelper.requireAdmin(request);
            Integer operatorId = authHelper.getUserId(request);
            Integer role = body.get("role");
            userService.updateUserRole(id, role, operatorId);
            return Result.success("角色更新成功");
        } catch (UnauthorizedException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
