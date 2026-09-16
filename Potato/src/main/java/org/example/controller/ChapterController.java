package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.result.Result;
import org.example.service.ChapterService;
import org.example.vo.ChapterContentVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 章节阅读接口（公开，付费章节自动校验购买）
 */
@RestController
@RequestMapping("/api/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    /** 阅读章节正文（免费直接读；付费需登录且已购买） */
    @GetMapping("/{id}")
    public Result<ChapterContentVO> read(@PathVariable Long id) {
        return Result.ok(chapterService.read(id));
    }
}
