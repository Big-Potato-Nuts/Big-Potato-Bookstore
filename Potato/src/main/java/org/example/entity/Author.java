package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作者表（用户入驻申请记录）
 */
@Data
@TableName("author")
public class Author {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 user.id（一对一） */
    private Long userId;

    /** 真实姓名（实名认证） */
    private String realName;

    /** 身份证号（展示时脱敏） */
    private String idCard;

    /** 作者简介 */
    private String intro;

    /** 审核状态：0待审核/1通过/2驳回 */
    private Integer auditStatus;

    /** 驳回原因 */
    private String auditReason;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 申请时间 */
    private LocalDateTime createTime;
}
