package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入书架请求
 */
@Data
public class BookshelfAddDTO {

    @NotNull(message = "小说 id 不能为空")
    private Long bookId;

    /** 是否收藏：1收藏（默认）/0仅加入书架 */
    private Integer isCollect = 1;
}
