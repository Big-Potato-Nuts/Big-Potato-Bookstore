package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节列表项（不含正文）
 */
@Data
public class ChapterVO {

    private Long id;
    private Long bookId;
    private String title;

    /** 是否免费：1免费/0付费 */
    private Integer isFree;

    private String isFreeText;

    /** 价格（单位：分） */
    private Integer price;

    /** 排序号 */
    private Integer sort;

    /** 字数 */
    private Integer words;

    private Integer readCount;
    private Integer payCount;
    private LocalDateTime createTime;
}
