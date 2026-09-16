package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员处理申诉参数
 */
@Data
public class AppealHandleDTO {

    /** 处理结果：1通过(解除处罚)/2驳回/3减轻处罚 */
    @NotNull(message = "处理结果不能为空")
    @Min(value = 1, message = "处理结果不合法")
    @Max(value = 3, message = "处理结果不合法")
    private Integer action;

    /** 管理员回复 */
    @Size(max = 500, message = "回复不能超过500字")
    private String adminReply;

    // ===== 减轻处罚时使用（action=3） =====
    /** 新的封禁功能位掩码 */
    private Integer banFlags;
    /** 新的封禁天数（-1=永久） */
    private Integer banDays;
    /** 新的封禁原因 */
    private String banReason;
}
