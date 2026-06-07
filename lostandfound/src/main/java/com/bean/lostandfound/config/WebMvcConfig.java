package com.bean.lostandfound.config;

import com.bean.lostandfound.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(
                        "/user/login",      // 排除登录接口
                        "/user/register",   // 排除注册接口
                        "/error",           // 排除错误页面
                        "/donation/notify",  // 排除支付宝异步通知接口
                        "/donation/return"   // 排除支付宝同步回调接口
                );
    }
}
