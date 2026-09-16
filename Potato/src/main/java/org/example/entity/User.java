package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("`user`")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 密码（BCrypt 密文），不参与 JSON 序列化 */
    @JsonIgnore
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 角色：0读者/1作者/2管理员 */
    private Integer role;

    /** 余额（单位：分） */
    private Integer balance;

    /** 状态：1正常/0禁用 */
    private Integer status;

    /** 封禁功能位掩码：bit0(1)=登录/全功能, bit1(2)=评论, bit2(4)=点赞, bit3(8)=阅读购买 */
    private Integer banFlags;

    /** 封禁到期时间（null=永久） */
    private LocalDateTime banExpireTime;

    /** 封禁原因 */
    private String banReason;

    private LocalDateTime createTime;
}
