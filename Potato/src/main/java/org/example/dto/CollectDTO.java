package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏/取消收藏请求
 */
@Data
public class CollectDTO {

    @NotNull(message = "小说 id 不能为空")
    private Long bookId;

    /** 是否收藏：1收藏/0取消 */
    @NotNull(message = "收藏状态不能为空")
    private Integer isCollect;
}
