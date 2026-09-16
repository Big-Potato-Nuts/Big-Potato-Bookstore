package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 模拟支付请求
 */
@Data
public class OrderPayDTO {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}
