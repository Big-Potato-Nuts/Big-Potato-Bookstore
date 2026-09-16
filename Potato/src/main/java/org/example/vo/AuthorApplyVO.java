package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作者入驻申请信息（身份证号脱敏展示）
 */
@Data
public class AuthorApplyVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;

    /** 身份证号（脱敏） */
    private String idCard;

    private String intro;

    /** 审核状态：0待审核/1通过/2驳回 */
    private Integer auditStatus;
    private String auditStatusText;

    private String auditReason;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
}
