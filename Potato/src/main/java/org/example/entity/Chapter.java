package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节表
 */
@Data
@TableName("chapter")
public class Chapter {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 book.id */
    private Long bookId;

    /** 章节标题 */
    private String title;

    /** 章节正文（文本存储） */
    private String content;

    /** 是否免费：1免费/0付费 */
    private Integer isFree;

    /** 价格（单位：分，付费章节生效） */
    private Integer price;

    /** 章节排序号 */
    private Integer sort;

    /** 正文字数 */
    private Integer words;

    /** 阅读次数 */
    private Integer readCount;

    /** 付费阅读次数 */
    private Integer payCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
