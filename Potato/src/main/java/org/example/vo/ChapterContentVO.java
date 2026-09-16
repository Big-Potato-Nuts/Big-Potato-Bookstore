package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节正文（阅读页返回，含上一章/下一章 id）
 */
@Data
public class ChapterContentVO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String title;
    private String content;

    /** 是否免费：1免费/0付费 */
    private Integer isFree;

    private Integer price;
    private Integer sort;
    private Integer words;
    private Integer readCount;
    private LocalDateTime createTime;

    /** 上一章 id（无则为 null） */
    private Long prevChapterId;

    /** 下一章 id（无则为 null） */
    private Long nextChapterId;
}
