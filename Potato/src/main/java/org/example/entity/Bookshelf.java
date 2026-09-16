package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 书架表（收藏 + 阅读记录）
 */
@Data
@TableName("bookshelf")
public class Bookshelf {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 user.id */
    private Long userId;

    /** 关联 book.id */
    private Long bookId;

    /** 最近阅读章节（阅读进度） */
    private Long readChapterId;

    /** 章节内阅读位置（书签） */
    private Integer readPosition;

    /** 是否收藏：1收藏/0仅阅读记录 */
    private Integer isCollect;

    /** 最近阅读时间 */
    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
