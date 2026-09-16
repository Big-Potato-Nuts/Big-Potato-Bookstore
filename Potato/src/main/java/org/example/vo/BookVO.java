package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说信息（含分类名、作者名、章节数等联表字段）
 */
@Data
public class BookVO {

    private Long id;
    private Long authorId;
    private Long categoryId;
    private String title;
    private String cover;
    private String intro;

    /** 状态：1连载中/2已完结/0已下架 */
    private Integer status;

    /** 状态中文名 */
    private String statusText;

    /** 是否推荐：1是/0否 */
    private Integer isRecommend;

    private Integer clickCount;
    private Integer collectCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 分类名 */
    private String categoryName;

    /** 作者昵称 */
    private String authorName;

    /** 作者用户 id（可跳转作者主页） */
    private Long authorUserId;

    /** 章节总数 */
    private Long chapterCount;

    /** 最新章节标题 */
    private String latestChapterTitle;

    /** 当前登录用户是否已收藏（详情接口返回） */
    private Boolean collected;

    /** 当前登录用户是否已购买整本（详情接口返回） */
    private Boolean purchased;
}
