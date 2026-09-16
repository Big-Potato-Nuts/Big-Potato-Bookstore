package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 关联 user.id（购买者） */
    private Long userId;

    /** 关联 book.id */
    private Long bookId;

    /** 关联 chapter.id（单章购买时非空） */
    private Long chapterId;

    /** 订单类型：1单章/2整本 */
    private Integer type;

    /** 订单金额（单位：分） */
    private Integer amount;

    /** 支付状态：0待支付/1已支付/2已关闭/3已退款 */
    private Integer payStatus;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 关闭时间（超时自动关闭） */
    private LocalDateTime closeTime;

    /** 下单时间 */
    private LocalDateTime createTime;
}
