package com.bean.lostandfound.interceptor;

import com.bean.lostandfound.utils.BaseContext;
import com.bean.lostandfound.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
/**
 * 登录检查拦截器
 */
@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截的是否是Controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 从请求头中获取token
        String token = request.getHeader(jwtUtil.getHeader());

        // 2. 判断token是否存在
        if (token == null || token.isEmpty()) {
            log.warn("请求头中未携带token: {}", request.getRequestURI());
            // 这里可以根据需求选择放行或返回401
            // 如果某些接口允许匿名访问，建议配合注解或路径排除逻辑
            // 暂时假设所有经过此拦截器的接口都需要登录，若未携带则视为非法
            // 注意：通常我们会配置 excludePathPatterns 来排除登录/注册接口
            return true; // 或者返回 false 并设置 response.setStatus(401)
        }

        try {
            // 3. 验证token有效性
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token无效或已过期: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 4. 解析token获取用户ID
            Integer userId = jwtUtil.getUserIdFromToken(token);

            // 5. 将用户ID存入ThreadLocal
            // BaseContext.setCurrentId 接收 Long 类型，所以进行转换
            BaseContext.setCurrentId(userId.longValue());

            log.debug("用户ID已存入ThreadLocal: {}", userId);
            return true;

        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 可选：处理后续逻辑
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 6. 重要：清理ThreadLocal，防止内存泄漏
        BaseContext.removeCurrentId();
    }
}
