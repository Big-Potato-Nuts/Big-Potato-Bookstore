package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建/编辑小说请求
 */
@Data
public class BookSaveDTO {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "书名不能为空")
    @Size(max = 100, message = "书名最长 100 个字符")
    private String title;

    @Size(max = 255, message = "封面地址过长")
    private String cover;

    @Size(max = 2000, message = "简介最长 2000 个字符")
    private String intro;
}
