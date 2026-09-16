package org.example.vo;

import lombok.Data;
import org.example.entity.User;

import java.time.LocalDateTime;

/**
 * 用户信息（不含密码）
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;

    /** 角色：0读者/1作者/2管理员 */
    private Integer role;

    /** 角色中文名 */
    private String roleText;

    /** 余额（单位：分） */
    private Integer balance;

    /** 状态：1正常/0禁用 */
    private Integer status;

    /** 状态中文名 */
    private String statusText;

    /** 封禁功能位掩码 */
    private Integer banFlags;

    /** 封禁到期时间 */
    private LocalDateTime banExpireTime;

    /** 封禁原因 */
    private String banReason;

    /** 封禁状态描述（如"评论封禁中，2026-09-23到期"） */
    private String banStatusText;

    private LocalDateTime createTime;

    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setBalance(user.getBalance());
        vo.setStatus(user.getStatus());
        vo.setBanFlags(user.getBanFlags());
        vo.setBanExpireTime(user.getBanExpireTime());
        vo.setBanReason(user.getBanReason());
        vo.setCreateTime(user.getCreateTime());
        vo.setRoleText(roleText(user.getRole()));
        vo.setStatusText(user.getStatus() != null && user.getStatus() == 1 ? "正常" : "禁用");
        vo.setBanStatusText(buildBanStatusText(user));
        return vo;
    }

    /** 构建封禁状态描述文本 */
    private static String buildBanStatusText(User user) {
        if (user.getBanFlags() == null || user.getBanFlags() == 0) {
            return "";
        }
        // 检查是否已过期
        if (user.getBanExpireTime() != null && user.getBanExpireTime().isBefore(LocalDateTime.now())) {
            return "封禁已过期";
        }
        StringBuilder sb = new StringBuilder();
        int flags = user.getBanFlags();
        if ((flags & 1) != 0) sb.append("禁止登录");
        if ((flags & 64) != 0) sb.append(sb.length() > 0 ? "、" : "").append("全功能限制");
        if ((flags & 2) != 0) sb.append(sb.length() > 0 ? "、" : "").append("评论封禁");
        if ((flags & 4) != 0) sb.append(sb.length() > 0 ? "、" : "").append("点赞封禁");
        if ((flags & 8) != 0) sb.append(sb.length() > 0 ? "、" : "").append("阅读购买封禁");
        if ((flags & 16) != 0) sb.append(sb.length() > 0 ? "、" : "").append("作者警告");
        if ((flags & 32) != 0) sb.append(sb.length() > 0 ? "、" : "").append("封禁写书");
        if (user.getBanExpireTime() == null) {
            sb.append("（永久）");
        } else {
            sb.append("（至 ").append(user.getBanExpireTime().toLocalDate()).append("）");
        }
        return sb.toString();
    }

    private static String roleText(Integer role) {
        if (role == null) {
            return "读者";
        }
        return switch (role) {
            case 1 -> "作者";
            case 2 -> "管理员";
            default -> "读者";
        };
    }
}
