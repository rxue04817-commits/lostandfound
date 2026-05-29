package com.bean.lostandfound.controller.user;

import com.bean.lostandfound.exception.BaseException;
import com.bean.lostandfound.pojo.dto.LoginDTO;
import com.bean.lostandfound.pojo.dto.UserDTO;
import com.bean.lostandfound.pojo.dto.UserProfileUpdateDTO;
import com.bean.lostandfound.pojo.dto.UserRegisterDTO;
import com.bean.lostandfound.pojo.entity.User;
import com.bean.lostandfound.pojo.vo.LoginResponseVO;
import com.bean.lostandfound.pojo.vo.UserInfoVO;
import com.bean.lostandfound.result.Result;
import com.bean.lostandfound.server.OssService;
import com.bean.lostandfound.server.UserService;
import com.bean.lostandfound.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private OssService ossService;
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录: {} ", loginDTO.getUsername());

        // 验证用户名和密码
        User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 生成JWT token，包含用户ID、用户名和角色
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 构造返回数据
        LoginResponseVO responseVO = new LoginResponseVO();
        responseVO.setToken(token);
        UserInfoVO userInfoVO=new UserInfoVO();
        BeanUtils.copyProperties(user,userInfoVO);
        responseVO.setUserInfoVO(userInfoVO);

        return Result.success(responseVO);
    }
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        /*
         * 。核心思想是后端提供退出接口供前端调用，前端负责删除本地存储的认证信息。*/
        try {
            // 从请求头获取用户ID
            String userIdStr = request.getHeader("user-id");

            if (userIdStr != null && !userIdStr.isEmpty()) {
                Integer userId = Integer.parseInt(userIdStr);
                userService.logout(userId);
                log.info("用户 {} 退出登录", userId);
            }

            return Result.success("退出登录成功");
        } catch (Exception e) {
            log.error("退出登录失败", e);
            return Result.error("退出登录失败");
        }
    }

//    //根据id查询
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Integer id){
        log.info("查询用户信息"+id);

        return Result.success(userService.getById(id));
    }
    /**
     * 用户更新自己的个人信息
     * 所有用户（包括管理员）都通过此接口更新自己的信息
     * JWT令牌中存储了当前用户的ID
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody UserProfileUpdateDTO profileUpdateDTO,
                                        HttpServletRequest request) {
        // 从token中获取当前用户ID
        String token = request.getHeader(jwtUtil.getHeader());
        System.out.println("当前用户的token"+token);
        if (token == null ) {
            return Result.error("未授权");
        }

        if (!jwtUtil.validateToken(token)) {
            return Result.error("token无效或已过期");
        }

        Integer currentUserId = jwtUtil.getUserIdFromToken(token);
        log.info("用户更新个人信息: {}", currentUserId);
        System.out.println(profileUpdateDTO);

        // 直接调用完整的更新方法，它会处理所有字段包括头像
        userService.updateUserProfile(currentUserId, profileUpdateDTO);

        return Result.success("个人信息更新成功");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        try {
            String generatedUsername = userService.register(userRegisterDTO);
            return Result.successWithUsername(generatedUsername);
        } catch (BaseException e) {
            return Result.error(e.getMessage());
        }
    }
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                       HttpServletRequest request) {
        try {
            // 验证token
            String token = request.getHeader(jwtUtil.getHeader());
            if (token == null) {
                return Result.error("未授权");
            }

            if (!jwtUtil.validateToken(token)) {
                return Result.error("token无效或已过期");
            }

            // 上传文件到OSS
            String fileUrl = ossService.uploadFile(file, "avatar/");

            // 这里应该更新用户头像URL到数据库
            Integer userId = jwtUtil.getUserIdFromToken(token);
            userService.updateUserProfile(userId, fileUrl);

            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
}

