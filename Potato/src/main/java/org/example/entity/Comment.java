package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表
 */
@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 user.id */
    private Long userId;

    /** 关联 book.id */
    private Long bookId;

    /** 评论内容 */
    private String content;

    /** 状态：1正常/0已屏蔽 */
    private Integer status;

    /** 评论时间 */
    private LocalDateTime createTime;
}
