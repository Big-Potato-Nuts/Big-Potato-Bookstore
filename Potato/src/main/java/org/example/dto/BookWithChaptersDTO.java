package org.example.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新建作品（含章节）请求
 */
@Data
public class BookWithChaptersDTO {

    @NotBlank(message = "书名不能为空")
    @Size(max = 100, message = "书名最长 100 个字符")
    private String title;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Size(max = 500, message = "封面 URL 最长 500 个字符")
    private String cover;

    @Size(max = 2000, message = "简介最长 2000 个字符")
    private String intro;

    /** 章节列表（可选，为空则只创建书籍） */
    @Valid
    private List<ChapterItem> chapters;

    @Data
    public static class ChapterItem {
        @NotBlank(message = "章节标题不能为空")
        @Size(max = 100, message = "章节标题最长 100 个字符")
        private String title;

        @NotBlank(message = "章节正文不能为空")
        private String content;

        /** 是否免费：1免费/0付费，默认 1 */
        private Integer isFree = 1;

        /** 价格（单位：分），付费章节需大于 0 */
        private Integer price;
    }
}
