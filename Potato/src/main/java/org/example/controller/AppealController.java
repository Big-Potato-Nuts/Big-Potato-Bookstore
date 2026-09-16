package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.result.PageResult;
import org.example.common.result.Result;
import org.example.dto.AppealCreateDTO;
import org.example.service.AppealService;
import org.example.vo.UserAppealVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户申诉接口（读者端，需登录）
 */
@RestController
@RequestMapping("/api/appeal")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;

    /** 发起申诉 */
    @PostMapping("/create")
    public Result<UserAppealVO> create(@Valid @RequestBody AppealCreateDTO dto) {
        return Result.ok("申诉提交成功", appealService.create(dto));
    }

    /** 我的申诉列表 */
    @GetMapping("/my")
    public Result<PageResult<UserAppealVO>> my(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.ok(appealService.myAppeals(page, size));
    }

    /** 是否有待处理申诉 */
    @GetMapping("/has-pending")
    public Result<Boolean> hasPending() {
        return Result.ok(appealService.hasPending());
    }
}
