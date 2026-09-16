package org.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.result.Result;
import org.example.dto.BookshelfAddDTO;
import org.example.dto.CollectDTO;
import org.example.dto.ProgressDTO;
import org.example.service.BookshelfService;
import org.example.vo.BookshelfVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 书架接口（需登录）：收藏、阅读记录、阅读进度
 */
@RestController
@RequestMapping("/api/bookshelf")
@RequiredArgsConstructor
public class BookshelfController {

    private final BookshelfService bookshelfService;

    /** 我的书架 */
    @GetMapping("/list")
    public Result<List<BookshelfVO>> list() {
        return Result.ok(bookshelfService.list(StpUtil.getLoginIdAsLong()));
    }

    /** 加入书架（默认收藏） */
    @PostMapping("/add")
    public Result<BookshelfVO> add(@Valid @RequestBody BookshelfAddDTO dto) {
        return Result.ok(bookshelfService.add(StpUtil.getLoginIdAsLong(), dto));
    }

    /** 手动上报阅读进度 */
    @PutMapping("/progress")
    public Result<Void> progress(@Valid @RequestBody ProgressDTO dto) {
        bookshelfService.recordProgress(StpUtil.getLoginIdAsLong(), dto);
        return Result.ok();
    }

    /** 收藏 / 取消收藏 */
    @PutMapping("/collect")
    public Result<Void> collect(@Valid @RequestBody CollectDTO dto) {
        bookshelfService.setCollect(StpUtil.getLoginIdAsLong(), dto);
        return Result.ok();
    }

    /** 从书架移除 */
    @DeleteMapping("/{bookId}")
    public Result<Void> remove(@PathVariable Long bookId) {
        bookshelfService.remove(StpUtil.getLoginIdAsLong(), bookId);
        return Result.ok();
    }
}
