package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 从文本导入章节请求
 */
@Data
public class ChapterImportDTO {

    /** 所属小说 id */
    @NotNull(message = "书籍 ID 不能为空")
    private Long bookId;

    /** 纯文本内容，按章节标题自动拆分 */
    @NotBlank(message = "文本内容不能为空")
    private String text;

    /** 是否免费：1免费/0付费，默认 1 */
    private Integer isFree = 1;

    /** 价格（单位：分），付费章节需大于 0 */
    private Integer price;
}
