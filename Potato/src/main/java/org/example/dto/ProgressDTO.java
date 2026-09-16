package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 阅读进度上报请求
 */
@Data
public class ProgressDTO {

    @NotNull(message = "小说 id 不能为空")
    private Long bookId;

    @NotNull(message = "章节 id 不能为空")
    private Long chapterId;

    /** 章节内阅读位置（书签），默认 0 */
    private Integer position = 0;
}
