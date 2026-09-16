package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模拟充值请求
 */
@Data
public class RechargeDTO {

    /** 充值金额（单位：分），一次最多 100000 分（1000 元） */
    @NotNull(message = "充值金额不能为空")
    @Min(value = 1, message = "充值金额至少为 1 分")
    @Max(value = 100000, message = "单次充值不超过 100000 分")
    private Integer amount;
}
