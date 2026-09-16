package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 书架项（含小说信息与阅读进度）
 */
@Data
public class BookshelfVO {

    private Long id;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private String cover;
    private String authorName;

    /** 小说状态：1连载中/2已完结/0已下架 */
    private Integer bookStatus;

    /** 最近阅读章节 id */
    private Long readChapterId;

    /** 最近阅读章节标题 */
    private String readChapterTitle;

    /** 章节内阅读位置（书签） */
    private Integer readPosition;

    /** 是否收藏：1收藏/0仅阅读记录 */
    private Integer isCollect;

    /** 最近阅读时间 */
    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
