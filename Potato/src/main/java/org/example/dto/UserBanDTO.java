package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户封禁参数
 */
@Data
public class UserBanDTO {

    /** 封禁功能位掩码：bit0(1)=禁止登录, bit1(2)=评论, bit2(4)=点赞, bit3(8)=阅读购买, bit4(16)=作者警告, bit5(32)=封禁写书, bit6(64)=全功能限制 */
    @NotNull(message = "封禁功能不能为空")
    @Min(value = 1, message = "封禁功能不能为0")
    @Max(value = 127, message = "封禁功能位掩码超出范围")
    private Integer banFlags;

    /** 封禁天数：-1=永久，1~3650=指定天数 */
    @NotNull(message = "封禁天数不能为空")
    @Min(value = -1, message = "封禁天数不能小于-1")
    @Max(value = 3650, message = "封禁天数不能超过3650天")
    private Integer banDays;

    /** 封禁原因 */
    @Size(max = 255, message = "封禁原因不能超过255字")
    private String banReason;
}
