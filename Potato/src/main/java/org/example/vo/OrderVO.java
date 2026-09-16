package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单信息（含书名、章节名）
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;

    /** 购买者用户名（管理员列表展示） */
    private String username;

    /** 购买者昵称（管理员列表展示） */
    private String nickname;

    private Long bookId;
    private String bookTitle;
    private Long chapterId;
    private String chapterTitle;

    /** 订单类型：1单章/2整本 */
    private Integer type;
    private String typeText;

    /** 订单金额（单位：分） */
    private Integer amount;

    /** 支付状态：0待支付/1已支付/2已关闭/3已退款 */
    private Integer payStatus;
    private String payStatusText;

    private LocalDateTime payTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
}
