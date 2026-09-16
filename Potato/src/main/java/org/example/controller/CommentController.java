package org.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.result.PageResult;
import org.example.common.result.Result;
import org.example.dto.CommentDTO;
import org.example.service.CommentService;
import org.example.vo.CommentVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论接口：查看公开，发表需登录
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 小说评论列表（仅正常评论） */
    @GetMapping("/book/{bookId}/comment")
    public Result<PageResult<CommentVO>> list(@PathVariable Long bookId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.ok(commentService.pageByBook(bookId, page, size));
    }

    /** 发表评论（需登录） */
    @PostMapping("/book/{bookId}/comment")
    public Result<CommentVO> add(@PathVariable Long bookId,
                                 @Valid @RequestBody CommentDTO dto) {
        return Result.ok("评论成功", commentService.add(StpUtil.getLoginIdAsLong(), bookId, dto));
    }

    /** 我的评论记录（需登录） */
    @GetMapping("/comment/my")
    public Result<PageResult<CommentVO>> my(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.ok(commentService.myComments(StpUtil.getLoginIdAsLong(), page, size));
    }
}
