package org.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 作者入驻审核请求
 */
@Data
public class AuditDTO {

    /** 是否通过 */
    @NotNull(message = "审核结果不能为空")
    private Boolean pass;

    /** 驳回原因（驳回时必填） */
    @Size(max = 255, message = "驳回原因最长 255 个字符")
    private String reason;
}
