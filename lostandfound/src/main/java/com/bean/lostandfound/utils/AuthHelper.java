package com.bean.lostandfound.utils;

import com.bean.lostandfound.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    @Autowired
    private JwtUtil jwtUtil;

    public String getToken(HttpServletRequest request) {
        return request.getHeader(jwtUtil.getHeader());
    }

    public void requireLogin(HttpServletRequest request) {
        String token = getToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("未授权，请先登录");
        }
    }

    public void requireAdmin(HttpServletRequest request) {
        requireLogin(request);
        Integer role = jwtUtil.getRoleFromToken(getToken(request));
        if (role == null || role != 1) {
            throw new UnauthorizedException("无权限访问");
        }
    }

    public Integer getUserId(HttpServletRequest request) {
        requireLogin(request);
        return jwtUtil.getUserIdFromToken(getToken(request));
    }

    public Integer getRole(HttpServletRequest request) {
        requireLogin(request);
        return jwtUtil.getRoleFromToken(getToken(request));
    }
}
