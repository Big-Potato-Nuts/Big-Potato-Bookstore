package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 封禁校验服务：检查用户功能封禁状态，自动清除已过期封禁
 */
@Service
@RequiredArgsConstructor
public class BanService {

    private final UserMapper userMapper;

    /**
     * 检查用户是否被封禁指定功能，若被封禁则抛出业务异常
     *
     * @param userId  用户ID
     * @param banFlag 封禁功能位（CommonConstant.BAN_COMMENT / BAN_LIKE / BAN_READ / BAN_LOGIN）
     * @param action  操作名称（用于异常提示，如"发表评论"）
     */
    public void checkBan(Long userId, int banFlag, String action) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 管理员不受封禁限制
        if (user.getRole() != null && user.getRole() == CommonConstant.ROLE_ADMIN) {
            return;
        }
        // 账号被禁用（status=0）等同于全功能封禁
        if (user.getStatus() != null && user.getStatus() == CommonConstant.USER_STATUS_DISABLED) {
            throw new BusinessException(403, "账号已被禁用，无法" + action);
        }
        if (user.getBanFlags() == null || user.getBanFlags() == 0) {
            return;
        }
        // 检查封禁是否已过期，过期则自动清除
        if (user.getBanExpireTime() != null && user.getBanExpireTime().isBefore(LocalDateTime.now())) {
            clearBan(user);
            return;
        }
        // 全功能限制（bit6）或禁止登录（bit0）或指定功能被封禁
        if ((user.getBanFlags() & CommonConstant.BAN_ALL_FUNCTIONS) != 0
                || (user.getBanFlags() & CommonConstant.BAN_LOGIN) != 0
                || (user.getBanFlags() & banFlag) != 0) {
            String reason = user.getBanReason() != null ? "，原因：" + user.getBanReason() : "";
            String expire = user.getBanExpireTime() == null ? "永久封禁"
                    : "封禁至 " + user.getBanExpireTime().toLocalDate();
            throw new BusinessException(403, "你已被" + expire + "，暂时无法" + action + reason);
        }
    }

    /**
     * 检查用户是否被封禁指定功能（不抛异常，返回 boolean）
     */
    public boolean isBanned(Long userId, int banFlag) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        if (user.getRole() != null && user.getRole() == CommonConstant.ROLE_ADMIN) {
            return false;
        }
        if (user.getStatus() != null && user.getStatus() == CommonConstant.USER_STATUS_DISABLED) {
            return true;
        }
        if (user.getBanFlags() == null || user.getBanFlags() == 0) {
            return false;
        }
        if (user.getBanExpireTime() != null && user.getBanExpireTime().isBefore(LocalDateTime.now())) {
            clearBan(user);
            return false;
        }
        return (user.getBanFlags() & CommonConstant.BAN_ALL_FUNCTIONS) != 0
                || (user.getBanFlags() & CommonConstant.BAN_LOGIN) != 0
                || (user.getBanFlags() & banFlag) != 0;
    }

    /**
     * 清除用户的封禁信息（封禁到期自动解除）
     */
    public void clearBan(User user) {
        user.setBanFlags(CommonConstant.BAN_NONE);
        user.setBanExpireTime(null);
        user.setBanReason(null);
        user.setStatus(CommonConstant.USER_STATUS_NORMAL);
        userMapper.updateById(user);
    }

    /**
     * 检查作者是否被封禁写书（禁止上传/编辑书籍和章节）
     */
    public void checkAuthorWriteBan(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getRole() != null && user.getRole() == CommonConstant.ROLE_ADMIN) {
            return;
        }
        if (user.getStatus() != null && user.getStatus() == CommonConstant.USER_STATUS_DISABLED) {
            throw new BusinessException(403, "账号已被禁用，无法进行创作");
        }
        if (user.getBanFlags() == null || user.getBanFlags() == 0) {
            return;
        }
        if (user.getBanExpireTime() != null && user.getBanExpireTime().isBefore(LocalDateTime.now())) {
            clearBan(user);
            return;
        }
        // 全功能限制 或 禁止登录 或 封禁写书
        if ((user.getBanFlags() & CommonConstant.BAN_ALL_FUNCTIONS) != 0
                || (user.getBanFlags() & CommonConstant.BAN_LOGIN) != 0
                || (user.getBanFlags() & CommonConstant.BAN_AUTHOR_WRITE) != 0) {
            String reason = user.getBanReason() != null ? "，原因：" + user.getBanReason() : "";
            String expire = user.getBanExpireTime() == null ? "永久封禁"
                    : "封禁至 " + user.getBanExpireTime().toLocalDate();
            throw new BusinessException(403, "你已被" + expire + "，暂时无法进行创作" + reason);
        }
    }

    /**
     * 检查作者是否处于警告状态（返回 true 表示警告中，新书需待审核）
     */
    public boolean isAuthorWarning(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getBanFlags() == null || user.getBanFlags() == 0) {
            return false;
        }
        if (user.getBanExpireTime() != null && user.getBanExpireTime().isBefore(LocalDateTime.now())) {
            clearBan(user);
            return false;
        }
        return (user.getBanFlags() & CommonConstant.BAN_AUTHOR_WARNING) != 0;
    }

    /**
     * 登录时校验：检查账号是否被封禁登录，过期则自动解除
     */
    public void checkLoginBan(User user) {
        if (user.getRole() != null && user.getRole() == CommonConstant.ROLE_ADMIN) {
            return;
        }
        // status=0 且有封禁信息时，检查是否过期
        if (user.getStatus() != null && user.getStatus() == CommonConstant.USER_STATUS_DISABLED) {
            // 如果有封禁到期时间且已过期，自动解除
            if (user.getBanExpireTime() != null && user.getBanExpireTime().isBefore(LocalDateTime.now())) {
                clearBan(user);
                return;
            }
            // 永久封禁或未到期
            String reason = user.getBanReason() != null ? "，原因：" + user.getBanReason() : "";
            String expire = user.getBanExpireTime() == null ? "永久封禁"
                    : "封禁至 " + user.getBanExpireTime().toLocalDate();
            throw new BusinessException(403, "账号已被" + expire + "，无法登录" + reason);
        }
        // status=1 但 banFlags 包含 BAN_LOGIN 的情况
        if (user.getBanFlags() != null && (user.getBanFlags() & CommonConstant.BAN_LOGIN) != 0) {
            if (user.getBanExpireTime() != null && user.getBanExpireTime().isBefore(LocalDateTime.now())) {
                clearBan(user);
                return;
            }
            String reason = user.getBanReason() != null ? "，原因：" + user.getBanReason() : "";
            String expire = user.getBanExpireTime() == null ? "永久封禁"
                    : "封禁至 " + user.getBanExpireTime().toLocalDate();
            throw new BusinessException(403, "账号已被" + expire + "，无法登录" + reason);
        }
    }
}
