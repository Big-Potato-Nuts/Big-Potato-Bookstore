package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分类新增/编辑请求
 */
@Data
public class CategorySaveDTO {

    @NotBlank(message = "分类名不能为空")
    @Size(max = 20, message = "分类名最长 20 个字符")
    private String name;

    /** 排序号（升序） */
    private Integer sort;

    /** 状态：1启用/0停用 */
    private Integer status;
}
