package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说表
 */
@Data
@TableName("book")
public class Book {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 author.id */
    private Long authorId;

    /** 关联 category.id */
    private Long categoryId;

    /** 书名 */
    private String title;

    /** 封面图片 URL */
    private String cover;

    /** 简介 */
    private String intro;

    /** 状态：1连载中/2已完结/0已下架 */
    private Integer status;

    /** 是否推荐：1是/0否 */
    private Integer isRecommend;

    /** 阅读量 */
    private Integer clickCount;

    /** 收藏数 */
    private Integer collectCount;

    private LocalDateTime createTime;

    /** 最近更新时间 */
    private LocalDateTime updateTime;
}
