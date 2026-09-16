package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户申诉信息（含用户名、管理员名）
 */
@Data
public class UserAppealVO {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;

    /** 申诉类型：1评论封禁/2账号封禁/3其他 */
    private Integer appealType;
    private String appealTypeText;

    private String reason;

    /** 状态：0待处理/1已通过/2已驳回/3已减轻处罚 */
    private Integer status;
    private String statusText;

    private String adminReply;
    private Long adminId;
    private String adminName;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}
