package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 作者入驻申请请求（简化实名认证）
 */
@Data
public class ApplyAuthorDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名最长 50 个字符")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^\\d{17}[0-9Xx]$", message = "身份证号格式不正确")
    private String idCard;

    @Size(max = 500, message = "作者简介最长 500 个字符")
    private String intro;
}
