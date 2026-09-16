package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作者端单本作品统计
 */
@Data
public class AuthorBookStatsVO {

    private Long bookId;
    private String title;

    /** 小说状态：1连载中/2已完结/0已下架 */
    private Integer status;
    private String statusText;

    private long chapterCount;
    private int clickCount;
    private int collectCount;

    /** 付费阅读次数（各章节 pay_count 合计） */
    private long payCount;

    /** 收益（该作品已支付订单金额合计，单位：分） */
    private long revenue;

    private LocalDateTime updateTime;
}
