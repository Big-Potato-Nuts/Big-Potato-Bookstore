package org.example.common.config;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 角色数据源实现：
 * 从数据库加载用户角色（0-读者 / 1-作者 / 2-管理员），
 * 供 StpUtil.checkRole("1") / checkRole("2") 等方法使用。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本项目未使用细粒度权限码，返回空
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId;
        try {
            userId = Long.valueOf(loginId.toString());
        } catch (NumberFormatException e) {
            return List.of();
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() == null) {
            return List.of();
        }
        return List.of(String.valueOf(user.getRole()));
    }
}
