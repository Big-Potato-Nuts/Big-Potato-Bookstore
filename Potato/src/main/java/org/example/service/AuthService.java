package org.example.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.dto.LoginDTO;
import org.example.dto.RegisterDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.vo.LoginVO;
import org.example.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务：注册、登录、退出、当前用户
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final BanService banService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 注册并自动登录 */
    public LoginVO register(RegisterDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                ? dto.getUsername() : dto.getNickname());
        user.setRole(CommonConstant.ROLE_READER);
        user.setBalance(0);
        user.setStatus(CommonConstant.USER_STATUS_NORMAL);
        userMapper.insert(user);

        // 回读用户，拿到数据库默认值（createTime 等）
        User saved = userMapper.selectById(user.getId());
        StpUtil.login(user.getId());
        return buildLoginVO(saved);
    }

    /** 登录 */
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        // 检查登录封禁（过期自动解除）
        banService.checkLoginBan(user);
        // 重新查询（可能因过期自动解除而更新）
        user = userMapper.selectById(user.getId());
        StpUtil.login(user.getId());
        return buildLoginVO(user);
    }

    /** 退出登录 */
    public void logout() {
        StpUtil.logout();
    }

    /** 当前登录用户信息 */
    public UserVO currentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(CommonConstant.USER_STATUS_NORMAL).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "账号不存在或已被禁用");
        }
        return UserVO.from(user);
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUser(UserVO.from(user));
        return vo;
    }
}
