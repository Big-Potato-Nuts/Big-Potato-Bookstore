package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说分类表
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名（玄幻/都市/言情等） */
    private String name;

    /** 排序号（升序） */
    private Integer sort;

    /** 状态：1启用/0停用 */
    private Integer status;

    private LocalDateTime createTime;
}
