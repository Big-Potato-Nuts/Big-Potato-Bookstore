package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.result.PageResult;
import org.example.common.result.Result;
import org.example.service.BookService;
import org.example.vo.BookVO;
import org.example.vo.ChapterVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小说浏览接口（公开）
 */
@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /** 分页浏览/搜索（categoryId 可选，keyword 匹配书名或作者名） */
    @GetMapping("/page")
    public Result<PageResult<BookVO>> page(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String keyword) {
        size = Math.min(size, 50);
        return Result.ok(bookService.pageBooks(page, size, categoryId, keyword));
    }

    /** 首页推荐位 */
    @GetMapping("/recommend")
    public Result<List<BookVO>> recommend(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(bookService.recommend(limit));
    }

    /** 排行榜：type=click(人气)/collect(收藏)/update(最近更新) */
    @GetMapping("/rank")
    public Result<List<BookVO>> rank(@RequestParam(defaultValue = "click") String type,
                                     @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(bookService.rank(type, limit));
    }

    /** 小说详情 */
    @GetMapping("/{id}")
    public Result<BookVO> detail(@PathVariable Long id) {
        return Result.ok(bookService.detail(id));
    }

    /** 章节目录（不含正文） */
    @GetMapping("/{id}/chapters")
    public Result<List<ChapterVO>> chapters(@PathVariable Long id) {
        return Result.ok(bookService.chapters(id));
    }
}
