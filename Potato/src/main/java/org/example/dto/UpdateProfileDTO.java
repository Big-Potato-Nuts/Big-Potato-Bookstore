package org.example.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人资料请求
 */
@Data
public class UpdateProfileDTO {

    @Size(max = 50, message = "昵称最长 50 个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;
}
