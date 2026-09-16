package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 小说推荐位设置请求
 */
@Data
public class BookRecommendDTO {

    /** 是否推荐位展示：1推荐 / 0取消推荐 */
    @NotNull(message = "推荐状态不能为空")
    @Min(value = 0, message = "推荐状态不合法")
    @Max(value = 1, message = "推荐状态不合法")
    private Integer isRecommend;
}
