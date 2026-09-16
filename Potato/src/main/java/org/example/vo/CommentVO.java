package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论信息（含用户昵称、头像、书名）
 */
@Data
public class CommentVO {

    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private Long bookId;
    private String bookTitle;
    private String content;

    /** 状态：1正常/0已屏蔽 */
    private Integer status;
    private String statusText;

    private LocalDateTime createTime;
}
