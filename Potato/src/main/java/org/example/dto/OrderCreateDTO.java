package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单请求（模拟购买）
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "小说 id 不能为空")
    private Long bookId;

    /** 单章购买时必填：章节 id */
    private Long chapterId;

    /** 订单类型：1单章/2整本 */
    @NotNull(message = "订单类型不能为空")
    @Min(value = 1, message = "订单类型不合法")
    @Max(value = 2, message = "订单类型不合法")
    private Integer type;
}
