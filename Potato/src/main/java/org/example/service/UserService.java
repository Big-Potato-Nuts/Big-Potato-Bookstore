package org.example.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.dto.RechargeDTO;
import org.example.dto.UpdatePasswordDTO;
import org.example.dto.UpdateProfileDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 读者个人中心服务：资料、密码、余额
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User requireCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(CommonConstant.USER_STATUS_NORMAL).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "账号不存在或已被禁用");
        }
        return user;
    }

    /** 修改个人资料 */
    public UserVO updateProfile(UpdateProfileDTO dto) {
        User user = requireCurrentUser();
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        userMapper.updateById(user);
        return UserVO.from(user);
    }

    /** 修改密码 */
    public void updatePassword(UpdatePasswordDTO dto) {
        User user = requireCurrentUser();
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    /** 模拟充值（充入固定金额即增加余额） */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> recharge(RechargeDTO dto) {
        User user = requireCurrentUser();
        user.setBalance(user.getBalance() + dto.getAmount());
        userMapper.updateById(user);
        Map<String, Object> data = new HashMap<>();
        data.put("balance", user.getBalance());
        data.put("recharged", dto.getAmount());
        return data;
    }

    /** 查询余额 */
    public Map<String, Object> balance() {
        User user = requireCurrentUser();
        Map<String, Object> data = new HashMap<>();
        data.put("balance", user.getBalance());
        return data;
    }
}
