package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建/编辑章节请求
 */
@Data
public class ChapterSaveDTO {

    /** 所属小说 id（新建必填） */
    private Long bookId;

    @NotBlank(message = "章节标题不能为空")
    @Size(max = 100, message = "章节标题最长 100 个字符")
    private String title;

    @NotBlank(message = "章节正文不能为空")
    private String content;

    /** 是否免费：1免费/0付费 */
    @NotNull(message = "是否免费不能为空")
    @Min(value = 0, message = "免费标记不合法")
    @Max(value = 1, message = "免费标记不合法")
    private Integer isFree;

    /** 价格（单位：分），付费章节必填且需大于 0 */
    @Min(value = 0, message = "价格不能为负数")
    private Integer price;

    /** 排序号，为空时自动排在最后 */
    private Integer sort;
}
