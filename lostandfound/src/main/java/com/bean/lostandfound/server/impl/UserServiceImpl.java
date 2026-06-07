package com.bean.lostandfound.server.impl;

import com.bean.lostandfound.exception.BaseException;
import com.bean.lostandfound.exception.NotFoundException;
import com.bean.lostandfound.exception.UnauthorizedException;
import com.bean.lostandfound.mapper.LostFoundMapper;
import com.bean.lostandfound.mapper.UserMapper;
import com.bean.lostandfound.pojo.dto.UserProfileUpdateDTO;
import com.bean.lostandfound.pojo.dto.UserRegisterDTO;
import com.bean.lostandfound.pojo.dto.UserSearchDTO;
import com.bean.lostandfound.pojo.entity.User;
import com.bean.lostandfound.pojo.vo.AdminUserDetailVO;
import com.bean.lostandfound.pojo.vo.UserInfoVO;
import com.bean.lostandfound.result.PageResult;
import com.bean.lostandfound.server.UserService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    LostFoundMapper lostFoundMapper;

    public User getById(Integer id) {
        return userMapper.getById(id);
    }

    public User login(String username, String password) {
        User user = userMapper.getByUsername(username);
        if (user == null) {
            return null;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BaseException("账号已被禁用，请联系管理员");
        }
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!user.getPassword().equals(encryptedPassword)) {
            return null;
        }
        return user;
    }

    public void logout(Integer userId) {
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
    }

    public void updateUserProfile(Integer userId, UserProfileUpdateDTO profileUpdateDTO) {
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }

        if (profileUpdateDTO.getPassword() != null && !profileUpdateDTO.getPassword().isEmpty()) {
            String encryptedPassword = DigestUtils.md5DigestAsHex(profileUpdateDTO.getPassword().getBytes());
            user.setPassword(encryptedPassword);
        }
        user.setUpdatedAt(LocalDateTime.now());
        BeanUtils.copyProperties(profileUpdateDTO, user, "id", "password", "role", "status");
        userMapper.updateById(user);
    }

    public String register(UserRegisterDTO userRegisterDTO) {
        String generatedUsername;
        int sequence = 100000;

        do {
            generatedUsername = String.valueOf(sequence);
            sequence++;
            User existingUser = userMapper.getByUsername(generatedUsername);
            if (existingUser == null) {
                break;
            }
        } while (true);

        User user = new User();
        BeanUtils.copyProperties(userRegisterDTO, user, "role");
        user.setUsername(generatedUsername);
        user.setPassword(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()));
        user.setRole(0);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return generatedUsername;
    }

    public void updateUserProfile(Integer userId, String avatarUrl) {
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public PageResult getUserList(UserSearchDTO userSearchDTO) {
        PageHelper.startPage(userSearchDTO.getPage(), userSearchDTO.getSize());
        List<User> users = userMapper.selectByCondition(userSearchDTO);
        List<UserInfoVO> voList = users.stream().map(this::toUserInfoVO).collect(Collectors.toList());
        Page<User> page = (Page<User>) users;
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    public AdminUserDetailVO getUserDetail(Integer id) {
        User user = userMapper.getById(id);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        AdminUserDetailVO vo = new AdminUserDetailVO();
        BeanUtils.copyProperties(user, vo);
        vo.setPostCount(lostFoundMapper.countByUserId(id));
        return vo;
    }

    @Override
    public void updateUserStatus(Integer id, Integer status, Integer operatorId) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BaseException("状态值无效");
        }
        User target = getExistingUser(id);
        if (id.equals(operatorId)) {
            throw new BaseException("不能禁用自己的账号");
        }
        if (status == 0 && target.getRole() == 1 && userMapper.countActiveAdmins() <= 1) {
            throw new BaseException("系统至少保留一名有效管理员");
        }
        target.setStatus(status);
        target.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(target);
    }

    @Override
    public void updateUserRole(Integer id, Integer role, Integer operatorId) {
        if (role == null || (role != 0 && role != 1)) {
            throw new BaseException("角色值无效");
        }
        User target = getExistingUser(id);
        if (id.equals(operatorId)) {
            throw new BaseException("不能修改自己的管理员身份");
        }
        if (role == 0 && target.getRole() == 1 && userMapper.countActiveAdmins() <= 1) {
            throw new BaseException("系统至少保留一名有效管理员");
        }
        target.setRole(role);
        target.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(target);
    }

    private User getExistingUser(Integer id) {
        User user = userMapper.getById(id);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        return user;
    }

    private UserInfoVO toUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
