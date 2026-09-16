package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.result.PageResult;
import org.example.common.result.Result;
import org.example.dto.AuditDTO;
import org.example.dto.AppealHandleDTO;
import org.example.dto.BookRecommendDTO;
import org.example.dto.BookStatusDTO;
import org.example.dto.CategorySaveDTO;
import org.example.dto.CommentStatusDTO;
import org.example.dto.UserBanDTO;
import org.example.dto.UserStatusDTO;
import org.example.entity.Category;
import org.example.service.AdminService;
import org.example.vo.AuthorApplyVO;
import org.example.vo.BookVO;
import org.example.vo.CommentVO;
import org.example.vo.OrderVO;
import org.example.vo.UserAppealVO;
import org.example.vo.UserVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员端接口（路由已拦截，需管理员角色）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== 用户管理 ====================

    /** 用户列表 */
    @GetMapping("/user/page")
    public Result<PageResult<UserVO>> pageUsers(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer role) {
        size = Math.min(size, 50);
        return Result.ok(adminService.pageUsers(page, size, keyword, role));
    }

    /** 用户详情（含处罚信息） */
    @GetMapping("/user/{id}")
    public Result<UserVO> userDetail(@PathVariable Long id) {
        return Result.ok(adminService.userDetail(id));
    }

    /** 禁用/启用用户 */
    @PutMapping("/user/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusDTO dto) {
        adminService.updateUserStatus(id, dto);
        return Result.ok("操作成功", null);
    }

    /** 自定义封禁用户（按功能位掩码+时长） */
    @PostMapping("/user/{id}/ban")
    public Result<Void> banUser(@PathVariable Long id, @Valid @RequestBody UserBanDTO dto) {
        adminService.banUser(id, dto);
        return Result.ok("封禁成功", null);
    }

    /** 解除用户封禁 */
    @PostMapping("/user/{id}/unban")
    public Result<Void> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return Result.ok("已解除封禁", null);
    }

    // ==================== 作者入驻审核 ====================

    /** 作者入驻申请列表（auditStatus: 0待审核/1通过/2驳回） */
    @GetMapping("/author/page")
    public Result<PageResult<AuthorApplyVO>> pageAuthors(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) Integer auditStatus) {
        size = Math.min(size, 50);
        return Result.ok(adminService.pageAuthors(page, size, auditStatus));
    }

    /** 审核作者入驻 */
    @PostMapping("/author/{id}/audit")
    public Result<Void> auditAuthor(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        adminService.auditAuthor(id, dto);
        return Result.ok("审核完成", null);
    }

    // ==================== 小说内容审核 ====================

    /** 全部小说列表 */
    @GetMapping("/book/page")
    public Result<PageResult<BookVO>> pageBooks(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status) {
        size = Math.min(size, 50);
        return Result.ok(adminService.pageBooks(page, size, keyword, status));
    }

    /** 上架/下架/完结小说 */
    @PutMapping("/book/{id}/status")
    public Result<Void> updateBookStatus(@PathVariable Long id, @Valid @RequestBody BookStatusDTO dto) {
        adminService.updateBookStatus(id, dto);
        return Result.ok("操作成功", null);
    }

    /** 设置/取消小说推荐位 */
    @PutMapping("/book/{id}/recommend")
    public Result<Void> updateBookRecommend(@PathVariable Long id, @Valid @RequestBody BookRecommendDTO dto) {
        adminService.updateBookRecommend(id, dto);
        return Result.ok("操作成功", null);
    }

    // ==================== 分类管理 ====================

    /** 全部分类（含停用） */
    @GetMapping("/category/list")
    public Result<List<Category>> listCategories() {
        return Result.ok(adminService.listAllCategories());
    }

    /** 新增分类 */
    @PostMapping("/category")
    public Result<Category> createCategory(@Valid @RequestBody CategorySaveDTO dto) {
        return Result.ok("新增成功", adminService.createCategory(dto));
    }

    /** 编辑分类 */
    @PutMapping("/category/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategorySaveDTO dto) {
        return Result.ok("保存成功", adminService.updateCategory(id, dto));
    }

    /** 删除分类 */
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.ok("删除成功", null);
    }

    // ==================== 订单管理 ====================

    /** 订单列表（orderNo 精确 / username 模糊 / payStatus / type） */
    @GetMapping("/order/page")
    public Result<PageResult<OrderVO>> pageOrders(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) String orderNo,
                                                  @RequestParam(required = false) String username,
                                                  @RequestParam(required = false) Integer payStatus,
                                                  @RequestParam(required = false) Integer type) {
        size = Math.min(size, 50);
        return Result.ok(adminService.pageOrders(page, size, orderNo, username, payStatus, type));
    }

    /** 订单详情（管理员可查看任意订单） */
    @GetMapping("/order/{orderNo}")
    public Result<OrderVO> orderDetail(@PathVariable String orderNo) {
        return Result.ok(adminService.orderDetail(orderNo));
    }

    // ==================== 评论管理 ====================

    /** 评论列表（按小说/用户名筛选） */
    @GetMapping("/comment/page")
    public Result<PageResult<CommentVO>> pageComments(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) Long bookId,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) Integer status) {
        size = Math.min(size, 50);
        return Result.ok(adminService.pageComments(page, size, bookId, keyword, status));
    }

    /** 屏蔽/恢复评论 */
    @PutMapping("/comment/{id}/status")
    public Result<Void> updateCommentStatus(@PathVariable Long id, @Valid @RequestBody CommentStatusDTO dto) {
        adminService.updateCommentStatus(id, dto);
        return Result.ok("操作成功", null);
    }

    /** 删除评论 */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        adminService.deleteComment(id);
        return Result.ok("删除成功", null);
    }

    // ==================== 书籍审核 ====================

    /** 待审核书籍列表 */
    @GetMapping("/book/pending")
    public Result<PageResult<BookVO>> pagePendingBooks(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String keyword) {
        size = Math.min(size, 50);
        return Result.ok(adminService.pagePendingBooks(page, size, keyword));
    }

    /** 书籍审核通过 */
    @PostMapping("/book/{id}/approve")
    public Result<Void> approveBook(@PathVariable Long id) {
        adminService.approveBook(id);
        return Result.ok("审核通过", null);
    }

    /** 书籍审核驳回 */
    @PostMapping("/book/{id}/reject")
    public Result<Void> rejectBook(@PathVariable Long id, @RequestParam(required = false) String reason) {
        adminService.rejectBook(id, reason);
        return Result.ok("已驳回", null);
    }

    // ==================== 用户申诉管理 ====================

    /** 申诉列表 */
    @GetMapping("/appeal/page")
    public Result<PageResult<UserAppealVO>> pageAppeals(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) Integer status) {
        size = Math.min(size, 50);
        return Result.ok(adminService.pageAppeals(page, size, status));
    }

    /** 处理申诉（通过/驳回/减轻处罚） */
    @PostMapping("/appeal/{id}/handle")
    public Result<Void> handleAppeal(@PathVariable Long id, @Valid @RequestBody AppealHandleDTO dto) {
        adminService.handleAppeal(id, dto);
        return Result.ok("处理完成", null);
    }
}
