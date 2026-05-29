package com.bean.lostandfound.server.impl;

import com.bean.lostandfound.exception.BaseException;
import com.bean.lostandfound.pojo.dto.UserProfileUpdateDTO;
import com.bean.lostandfound.pojo.dto.UserRegisterDTO;
import com.bean.lostandfound.pojo.entity.User;
import com.bean.lostandfound.mapper.UserMapper;
import com.bean.lostandfound.server.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    public User getById(Integer id) {
        return userMapper.getById(id);
    }


    public User login(String username, String password) {
        // 根据用户名查询用户
        User user = userMapper.getByUsername(username);
        if (user == null) {
            return null;
        }
        // 对密码进行MD5加密后比较
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!user.getPassword().equals(encryptedPassword)) {
            return null;
        }
        return user;
    }
    public void logout(Integer userId) {
        // 查询用户是否存在
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }

        // 对于基于前端删除Token的简单退出方案，后端无需特殊处理
        // 只需记录日志或执行其他业务逻辑
    }

    public void updateUserProfile(Integer userId, UserProfileUpdateDTO profileUpdateDTO) {
        // 查询用户是否存在
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }

        // 使用属性拷贝更新用户信息
        // 注意：需要排除null值，只更新非null字段
        if (profileUpdateDTO.getPassword() != null && !profileUpdateDTO.getPassword().isEmpty()) {
            // 对新密码进行MD5加密
            String encryptedPassword = DigestUtils.md5DigestAsHex(profileUpdateDTO.getPassword().getBytes());
            user.setPassword(encryptedPassword);
        }
        user.setUpdatedAt(LocalDateTime.now());
        // 属性拷贝（排除password字段，因为我们已经特殊处理了）
        BeanUtils.copyProperties(profileUpdateDTO, user, "id", "password");

        // 更新数据库
        userMapper.updateById(user);
    }

    public String register(UserRegisterDTO userRegisterDTO) {
        // 生成自增序列用户名
        String generatedUsername;
        int sequence = 100000; // 起始序列号

        do {
            generatedUsername = "student" + sequence;
            sequence++;
            // 检查用户名是否已存在
            User existingUser = userMapper.getByUsername(generatedUsername);
            if (existingUser == null) {
                break; // 找到唯一用户名，跳出循环
            }
        } while (true); // 理论上不会无限循环，因为序列号会不断增加

        // 验证role值的有效性
        if (userRegisterDTO.getRole() != null &&
                userRegisterDTO.getRole() != 0 &&
                userRegisterDTO.getRole() != 1) {
            throw new BaseException("role值无效，只能为0（普通用户）或1（管理员）");
        }

        // 创建新用户
        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user);

        // 设置生成的用户名
        user.setUsername(generatedUsername);

        // 对密码进行MD5加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes());
        user.setPassword(encryptedPassword);
        // 如果前端没有传递role，则默认为普通用户(0)
        if (user.getRole() == null) {
            user.setRole(0);
        }
        user.setCreatedAt(LocalDateTime.now());
        // 设置默认状态为启用(1)
        user.setStatus(1);

        // 保存到数据库
        userMapper.insert(user);

        // 返回生成的用户名
        return generatedUsername;
    }
    public void updateUserProfile(Integer userId, String avatarUrl) {
        // 查询用户是否存在
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }

        // 只更新头像URL
        user.setAvatar(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());

        // 更新数据库
        userMapper.updateById(user);
    }


}
