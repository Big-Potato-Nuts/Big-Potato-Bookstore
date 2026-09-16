package org.example.vo;

import lombok.Data;

import java.util.List;

/**
 * 作者端统计（含单本统计与合计）
 */
@Data
public class AuthorStatsVO {

    /** 作品数量 */
    private int bookCount;

    /** 总阅读量 */
    private long totalClick;

    /** 总收藏数 */
    private long totalCollect;

    /** 总付费阅读次数 */
    private long totalPayCount;

    /** 总收益（单位：分） */
    private long totalRevenue;

    private List<AuthorBookStatsVO> books;
}
