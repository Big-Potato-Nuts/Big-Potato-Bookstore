package org.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.result.Result;
import org.example.dto.ApplyAuthorDTO;
import org.example.dto.BookSaveDTO;
import org.example.dto.BookStatusDTO;
import org.example.dto.BookWithChaptersDTO;
import org.example.dto.ChapterImportDTO;
import org.example.dto.ChapterSaveDTO;
import org.example.service.AuthorService;
import org.example.vo.AuthorApplyVO;
import org.example.vo.AuthorStatsVO;
import org.example.vo.BookVO;
import org.example.vo.ChapterVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 作者端接口
 *  - 入驻申请：登录即可；
 *  - 作品/章节/统计：需通过入驻审核的作者角色（路由已拦截）。
 */
@RestController
@RequestMapping("/api/author")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    /** 提交/重新提交入驻申请（登录即可） */
    @PostMapping("/apply")
    public Result<AuthorApplyVO> apply(@Valid @RequestBody ApplyAuthorDTO dto) {
        return Result.ok("申请提交成功", authorService.apply(dto));
    }

    /** 入驻申请状态（登录即可） */
    @GetMapping("/apply/status")
    public Result<AuthorApplyVO> applyStatus() {
        return Result.ok(authorService.applyStatus());
    }

    /** 我的作品列表 */
    @GetMapping("/book/list")
    public Result<List<BookVO>> myBooks() {
        return Result.ok(authorService.myBooks());
    }

    /** 新建小说 */
    @PostMapping("/book")
    public Result<BookVO> createBook(@Valid @RequestBody BookSaveDTO dto) {
        return Result.ok("创建成功", authorService.createBook(dto));
    }

    /** 编辑小说 */
    @PutMapping("/book/{id}")
    public Result<BookVO> updateBook(@PathVariable Long id, @Valid @RequestBody BookSaveDTO dto) {
        return Result.ok("保存成功", authorService.updateBook(id, dto));
    }

    /** 上架/完结/下架 */
    @PutMapping("/book/{id}/status")
    public Result<Void> updateBookStatus(@PathVariable Long id, @Valid @RequestBody BookStatusDTO dto) {
        authorService.updateBookStatus(id, dto);
        return Result.ok("操作成功", null);
    }

    /** 删除小说（连同章节） */
    @DeleteMapping("/book/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        authorService.deleteBook(id);
        return Result.ok("删除成功", null);
    }

    /** 重新提交审核（驳回后修改并重新提交） */
    @PostMapping("/book/{id}/resubmit")
    public Result<BookVO> resubmitBook(@PathVariable Long id) {
        return Result.ok("已重新提交审核", authorService.resubmitBook(id));
    }

    /** 新建作品（含章节），一次性创建书籍和所有章节 */
    @PostMapping("/book/with-chapters")
    public Result<BookVO> createBookWithChapters(@Valid @RequestBody BookWithChaptersDTO dto) {
        return Result.ok("创建成功", authorService.createBookWithChapters(dto));
    }

    /** 从纯文本导入章节，按「第X章」格式自动拆分 */
    @PostMapping("/chapter/import")
    public Result<Integer> importChapters(@Valid @RequestBody ChapterImportDTO dto) {
        int count = authorService.importChapters(dto);
        return Result.ok("成功导入 " + count + " 章", count);
    }

    /** 作品章节列表 */
    @GetMapping("/book/{bookId}/chapter/list")
    public Result<List<ChapterVO>> chapters(@PathVariable Long bookId) {
        return Result.ok(authorService.chapters(bookId));
    }

    /** 新建章节 */
    @PostMapping("/chapter")
    public Result<ChapterVO> createChapter(@Valid @RequestBody ChapterSaveDTO dto) {
        return Result.ok("发布成功", authorService.createChapter(dto));
    }

    /** 编辑章节 */
    @PutMapping("/chapter/{id}")
    public Result<ChapterVO> updateChapter(@PathVariable Long id, @Valid @RequestBody ChapterSaveDTO dto) {
        return Result.ok("保存成功", authorService.updateChapter(id, dto));
    }

    /** 删除章节 */
    @DeleteMapping("/chapter/{id}")
    public Result<Void> deleteChapter(@PathVariable Long id) {
        authorService.deleteChapter(id);
        return Result.ok("删除成功", null);
    }

    /** 数据看板（收益可按支付时间范围筛选，格式 yyyy-MM-dd） */
    @GetMapping("/stats")
    public Result<AuthorStatsVO> stats(@RequestParam(required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.ok(authorService.stats(start, end));
    }
}
