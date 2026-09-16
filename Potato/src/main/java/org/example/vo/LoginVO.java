package org.example.vo;

import lombok.Data;

/**
 * 登录/注册成功返回：令牌 + 用户信息
 */
@Data
public class LoginVO {

    /** Sa-Token 访问令牌，前端放入请求头 Authorization */
    private String token;

    private UserVO user;
}
