package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内通知
 */
@Data
public class NotificationVO {

    private Long id;
    private String type;
    private String title;
    private String content;

    /** 是否已读：1已读/0未读 */
    private Integer isRead;

    private LocalDateTime createTime;
}
