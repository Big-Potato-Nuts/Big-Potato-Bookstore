package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内通知表（增量表，见 sql/upgrade_notification.sql）
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户 id */
    private Long userId;

    /** 通知类型：SYSTEM/PURCHASE/AUDIT */
    private String type;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 是否已读：1已读/0未读 */
    private Integer isRead;

    private LocalDateTime createTime;
}
