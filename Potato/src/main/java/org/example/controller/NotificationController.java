package org.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.example.common.result.PageResult;
import org.example.common.result.Result;
import org.example.service.NotificationService;
import org.example.vo.NotificationVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 站内通知接口（需登录）
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** 通知列表 */
    @GetMapping("/list")
    public Result<PageResult<NotificationVO>> list(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.ok(notificationService.page(StpUtil.getLoginIdAsLong(), page, size));
    }

    /** 未读通知数 */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount() {
        long count = notificationService.unreadCount(StpUtil.getLoginIdAsLong());
        return Result.ok(Map.of("unreadCount", count));
    }

    /** 标记单条已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(StpUtil.getLoginIdAsLong(), id);
        return Result.ok();
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead(StpUtil.getLoginIdAsLong());
        return Result.ok();
    }
}
