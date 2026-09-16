package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发起申诉参数
 */
@Data
public class AppealCreateDTO {

    /** 申诉类型：1评论封禁/2账号封禁/3其他 */
    @NotNull(message = "申诉类型不能为空")
    @Min(value = 1, message = "申诉类型不合法")
    @Max(value = 3, message = "申诉类型不合法")
    private Integer appealType;

    /** 申诉理由 */
    @NotBlank(message = "申诉理由不能为空")
    @Size(max = 500, message = "申诉理由不能超过500字")
    private String reason;
}
