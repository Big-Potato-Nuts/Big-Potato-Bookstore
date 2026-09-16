package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户申诉表
 */
@Data
@TableName("user_appeal")
public class UserAppeal {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申诉用户ID */
    private Long userId;

    /** 申诉类型：1评论封禁/2账号封禁/3其他 */
    private Integer appealType;

    /** 申诉理由 */
    private String reason;

    /** 状态：0待处理/1已通过(解除处罚)/2已驳回/3已减轻处罚 */
    private Integer status;

    /** 管理员回复 */
    private String adminReply;

    /** 处理管理员ID */
    private Long adminId;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 申诉时间 */
    private LocalDateTime createTime;
}
