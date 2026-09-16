package org.example.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.exception.BusinessException;
import org.example.common.result.PageResult;
import org.example.common.util.MaskUtil;
import org.example.dto.AuditDTO;
import org.example.dto.BookStatusDTO;
import org.example.dto.CategorySaveDTO;
import org.example.dto.CommentStatusDTO;
import org.example.dto.AppealHandleDTO;
import org.example.dto.UserBanDTO;
import org.example.dto.UserStatusDTO;
import org.example.entity.Author;
import org.example.entity.Book;
import org.example.entity.Category;
import org.example.entity.Chapter;
import org.example.entity.Comment;
import org.example.entity.Orders;
import org.example.entity.User;
import org.example.entity.UserAppeal;
import org.example.mapper.AuthorMapper;
import org.example.mapper.BookMapper;
import org.example.mapper.CategoryMapper;
import org.example.mapper.ChapterMapper;
import org.example.mapper.CommentMapper;
import org.example.mapper.OrdersMapper;
import org.example.mapper.UserAppealMapper;
import org.example.mapper.UserMapper;
import org.example.vo.AuthorApplyVO;
import org.example.vo.BookVO;
import org.example.vo.CommentVO;
import org.example.vo.OrderVO;
import org.example.vo.UserAppealVO;
import org.example.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员端服务：用户管理、作者审核、内容审核、分类管理、订单管理、评论管理
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final AuthorMapper authorMapper;
    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final OrdersMapper ordersMapper;
    private final CommentMapper commentMapper;
    private final ChapterMapper chapterMapper;
    private final UserAppealMapper appealMapper;
    private final NotificationService notificationService;

    // ==================== 用户管理 ====================

    /** 用户列表（按用户名/昵称检索，按角色筛选） */
    public PageResult<UserVO> pageUsers(int page, int size, String keyword, Integer role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword.trim())
                    .or().like(User::getNickname, keyword.trim()));
        }
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getId);
        IPage<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result, result.getRecords().stream().map(UserVO::from).toList());
    }

    /** 用户详情（含处罚信息） */
    public UserVO userDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return UserVO.from(user);
    }

    /** 禁用/启用用户 */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long targetId, UserStatusDTO dto) {
        User target = userMapper.selectById(targetId);
        if (target == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (target.getRole() == CommonConstant.ROLE_ADMIN) {
            throw new BusinessException(400, "不能操作管理员账号");
        }
        if (dto.getStatus() == CommonConstant.USER_STATUS_DISABLED
                && target.getId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException(400, "不能禁用当前登录账号");
        }
        target.setStatus(dto.getStatus());
        // 禁用时同步设置全功能永久封禁，启用时清除封禁信息
        if (dto.getStatus() == CommonConstant.USER_STATUS_DISABLED) {
            target.setBanFlags(CommonConstant.BAN_LOGIN);
            target.setBanExpireTime(null);
            target.setBanReason("管理员禁用账号");
        } else {
            target.setBanFlags(CommonConstant.BAN_NONE);
            target.setBanExpireTime(null);
            target.setBanReason(null);
        }
        userMapper.updateById(target);
    }

    /** 自定义封禁用户（按功能位掩码 + 时长） */
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long targetId, UserBanDTO dto) {
        User target = userMapper.selectById(targetId);
        if (target == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (target.getRole() == CommonConstant.ROLE_ADMIN) {
            throw new BusinessException(400, "不能封禁管理员账号");
        }
        if (target.getId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException(400, "不能封禁当前登录账号");
        }
        target.setBanFlags(dto.getBanFlags());
        if (dto.getBanDays() != null && dto.getBanDays() >= 0) {
            target.setBanExpireTime(LocalDateTime.now().plusDays(dto.getBanDays()));
        } else {
            target.setBanExpireTime(null); // 永久
        }
        target.setBanReason(dto.getBanReason());
        // 如果封禁包含登录(bit0)，则同步将 status 设为禁用
        if ((dto.getBanFlags() & CommonConstant.BAN_LOGIN) != 0) {
            target.setStatus(CommonConstant.USER_STATUS_DISABLED);
        }
        userMapper.updateById(target);

        // 发送站内通知
        String banDesc = buildBanDesc(dto.getBanFlags(), dto.getBanDays());
        notificationService.create(target.getId(), "BAN", "账号处罚通知",
                "你的账号受到处罚：\n" + banDesc + "\n原因：" + (dto.getBanReason() != null ? dto.getBanReason() : "未填写")
                        + "\n如有异议请联系管理员。");
    }

    /** 解除用户封禁 */
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long targetId) {
        User target = userMapper.selectById(targetId);
        if (target == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (target.getRole() == CommonConstant.ROLE_ADMIN) {
            throw new BusinessException(400, "不能操作管理员账号");
        }
        target.setBanFlags(CommonConstant.BAN_NONE);
        target.setBanExpireTime(null);
        target.setBanReason(null);
        target.setStatus(CommonConstant.USER_STATUS_NORMAL);
        userMapper.updateById(target);

        notificationService.create(target.getId(), "BAN", "封禁解除通知",
                "你的账号封禁已解除，相关功能恢复正常使用。");
    }

    /** 构建封禁描述文本 */
    private String buildBanDesc(int banFlags, Integer banDays) {
        StringBuilder sb = new StringBuilder("封禁功能：");
        if ((banFlags & CommonConstant.BAN_LOGIN) != 0) sb.append("登录(全功能) ");
        if ((banFlags & CommonConstant.BAN_COMMENT) != 0) sb.append("评论 ");
        if ((banFlags & CommonConstant.BAN_LIKE) != 0) sb.append("点赞 ");
        if ((banFlags & CommonConstant.BAN_READ) != 0) sb.append("阅读购买 ");
        sb.append("；封禁时长：");
        if (banDays == null || banDays < 0) {
            sb.append("永久");
        } else {
            sb.append(banDays).append("天");
        }
        return sb.toString();
    }

    // ==================== 作者入驻审核 ====================

    /** 作者入驻申请列表 */
    public PageResult<AuthorApplyVO> pageAuthors(int page, int size, Integer auditStatus) {
        IPage<AuthorApplyVO> result = authorMapper.selectApplyPage(new Page<>(page, size), auditStatus);
        result.getRecords().forEach(a -> a.setIdCard(MaskUtil.maskIdCard(a.getIdCard())));
        result.getRecords().forEach(this::fillAuditText);
        return PageResult.of(result);
    }

    /** 审核作者入驻：通过则升级为作者角色，驳回需填写原因 */
    @Transactional(rollbackFor = Exception.class)
    public void auditAuthor(Long authorId, AuditDTO dto) {
        Author author = authorMapper.selectById(authorId);
        if (author == null) {
            throw new BusinessException(404, "申请记录不存在");
        }
        User user = userMapper.selectById(author.getUserId());
        if (user == null) {
            throw new BusinessException(404, "申请人不存在");
        }
        author.setAuditTime(LocalDateTime.now());
        if (Boolean.TRUE.equals(dto.getPass())) {
            author.setAuditStatus(CommonConstant.AUDIT_PASSED);
            author.setAuditReason(null);
            // 升级为作者角色
            user.setRole(CommonConstant.ROLE_AUTHOR);
            userMapper.updateById(user);
            notificationService.create(user.getId(), "AUDIT", "入驻审核通过",
                    "恭喜！你的作者入驻申请已通过审核，现在可以创建作品啦。");
        } else {
            if (dto.getReason() == null || dto.getReason().isBlank()) {
                throw new BusinessException(400, "驳回时请填写驳回原因");
            }
            author.setAuditStatus(CommonConstant.AUDIT_REJECTED);
            author.setAuditReason(dto.getReason());
            notificationService.create(user.getId(), "AUDIT", "入驻审核被驳回",
                    "很遗憾，你的入驻申请未通过：\n" + dto.getReason() + "\n可修改资料后重新提交。");
        }
        authorMapper.updateById(author);
    }

    // ==================== 小说内容审核 ====================

    /** 全部小说列表（可按书名/作者、状态筛选） */
    public PageResult<BookVO> pageBooks(int page, int size, String keyword, Integer status) {
        IPage<BookVO> result = bookMapper.selectBookPage(new Page<>(page, size),
                null, keyword, status, "update", null, null, false);
        result.getRecords().forEach(vo -> vo.setStatusText(bookStatusText(vo.getStatus())));
        return PageResult.of(result);
    }

    /** 上架/下架/完结小说 */
    @Transactional(rollbackFor = Exception.class)
    public void updateBookStatus(Long bookId, BookStatusDTO dto) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(404, "小说不存在");
        }
        book.setStatus(dto.getStatus());
        bookMapper.updateById(book);
        // 通知作者
        Author author = authorMapper.selectById(book.getAuthorId());
        if (author != null) {
            String action = switch (dto.getStatus()) {
                case 0 -> "下架";
                case 2 -> "完结";
                default -> "上架";
            };
            notificationService.create(author.getUserId(), "AUDIT", "作品状态变更",
                    "你的作品《" + book.getTitle() + "》已被管理员" + action + "。");
        }
    }

    // ==================== 分类管理 ====================

    /** 全部分类（含停用） */
    public List<Category> listAllCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    /** 新增分类 */
    @Transactional(rollbackFor = Exception.class)
    public Category createCategory(CategorySaveDTO dto) {
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, dto.getName()));
        if (count > 0) {
            throw new BusinessException(400, "分类名已存在");
        }
        Category category = new Category();
        category.setName(dto.getName());
        category.setSort(dto.getSort() == null ? 0 : dto.getSort());
        category.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        categoryMapper.insert(category);
        return category;
    }

    /** 编辑分类 */
    @Transactional(rollbackFor = Exception.class)
    public Category updateCategory(Long categoryId, CategorySaveDTO dto) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, dto.getName())
                .ne(Category::getId, categoryId));
        if (count > 0) {
            throw new BusinessException(400, "分类名已存在");
        }
        category.setName(dto.getName());
        if (dto.getSort() != null) {
            category.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }
        categoryMapper.updateById(category);
        return category;
    }

    /** 删除分类（分类下存在小说时禁止删除） */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        Long bookCount = bookMapper.selectCount(new LambdaQueryWrapper<Book>()
                .eq(Book::getCategoryId, categoryId));
        if (bookCount > 0) {
            throw new BusinessException(400, "该分类下存在小说，无法删除，可改为停用");
        }
        categoryMapper.deleteById(categoryId);
    }

    // ==================== 订单管理 ====================

    /** 订单列表（按订单号/用户名/支付状态/类型检索） */
    public PageResult<OrderVO> pageOrders(int page, int size, String orderNo, String username,
                                          Integer payStatus, Integer type) {
        IPage<OrderVO> result = ordersMapper.selectOrderPage(new Page<>(page, size),
                blankToNull(orderNo), null, blankToNull(username), payStatus, type);
        result.getRecords().forEach(this::fillOrderText);
        return PageResult.of(result);
    }

    /** 订单详情（管理员可查看任意订单，不校验所属用户） */
    public OrderVO orderDetail(String orderNo) {
        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setBookId(order.getBookId());
        vo.setChapterId(order.getChapterId());
        vo.setType(order.getType());
        vo.setAmount(order.getAmount());
        vo.setPayStatus(order.getPayStatus());
        vo.setPayTime(order.getPayTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setCreateTime(order.getCreateTime());
        Book book = bookMapper.selectById(order.getBookId());
        if (book != null) {
            vo.setBookTitle(book.getTitle());
        }
        if (order.getChapterId() != null) {
            Chapter chapter = chapterMapper.selectById(order.getChapterId());
            if (chapter != null) {
                vo.setChapterTitle(chapter.getTitle());
            }
        }
        // 填充用户名和昵称
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }
        fillOrderText(vo);
        return vo;
    }

    // ==================== 评论管理 ====================

    /** 评论列表（按小说/用户名筛选） */
    public PageResult<CommentVO> pageComments(int page, int size, Long bookId, String keyword, Integer status) {
        IPage<CommentVO> result = commentMapper.selectCommentPage(new Page<>(page, size),
                bookId, null, blankToNull(keyword), status);
        result.getRecords().forEach(vo -> vo.setStatusText(
                Integer.valueOf(CommonConstant.COMMENT_NORMAL).equals(vo.getStatus()) ? "正常" : "已屏蔽"));
        return PageResult.of(result);
    }

    /** 屏蔽/恢复评论 */
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentStatus(Long commentId, CommentStatusDTO dto) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        comment.setStatus(dto.getStatus());
        commentMapper.updateById(comment);
    }

    /** 删除评论 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        commentMapper.deleteById(commentId);
    }

    // ==================== 书籍审核 ====================

    /** 待审核书籍列表（status=3） */
    public PageResult<BookVO> pagePendingBooks(int page, int size, String keyword) {
        IPage<BookVO> result = bookMapper.selectBookPage(new Page<>(page, size),
                null, blankToNull(keyword), CommonConstant.BOOK_STATUS_PENDING, "update", null, null, false);
        result.getRecords().forEach(vo -> vo.setStatusText(bookStatusText(vo.getStatus())));
        return PageResult.of(result);
    }

    /** 书籍审核通过（status 3→1） */
    @Transactional(rollbackFor = Exception.class)
    public void approveBook(Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        if (!Integer.valueOf(CommonConstant.BOOK_STATUS_PENDING).equals(book.getStatus())) {
            throw new BusinessException(400, "仅待审核书籍可以通过");
        }
        book.setStatus(CommonConstant.BOOK_STATUS_SERIALIZING);
        bookMapper.updateById(book);
        // 通知作者
        Author author = authorMapper.selectById(book.getAuthorId());
        if (author != null) {
            notificationService.create(author.getUserId(), "AUDIT", "作品审核通过",
                    "你的作品《" + book.getTitle() + "》已通过审核，现在读者可以看到了。");
        }
    }

    /** 书籍审核驳回（status 3→0） */
    @Transactional(rollbackFor = Exception.class)
    public void rejectBook(Long bookId, String reason) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        if (!Integer.valueOf(CommonConstant.BOOK_STATUS_PENDING).equals(book.getStatus())) {
            throw new BusinessException(400, "仅待审核书籍可以驳回");
        }
        book.setStatus(CommonConstant.BOOK_STATUS_OFF);
        bookMapper.updateById(book);
        // 通知作者
        Author author = authorMapper.selectById(book.getAuthorId());
        if (author != null) {
            notificationService.create(author.getUserId(), "AUDIT", "作品审核被驳回",
                    "你的作品《" + book.getTitle() + "》审核未通过：\n" + (reason != null ? reason : "未填写原因")
                            + "\n可修改后重新提交审核。");
        }
    }

    // ==================== 用户申诉管理 ====================

    /** 申诉列表（按状态筛选） */
    public PageResult<UserAppealVO> pageAppeals(int page, int size, Integer status) {
        LambdaQueryWrapper<UserAppeal> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(UserAppeal::getStatus, status);
        }
        wrapper.orderByDesc(UserAppeal::getCreateTime);
        IPage<UserAppeal> result = appealMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserAppealVO> records = result.getRecords().stream().map(this::toAppealVO).toList();
        return PageResult.of(result, records);
    }

    /** 处理申诉（通过/驳回/减轻处罚） */
    @Transactional(rollbackFor = Exception.class)
    public void handleAppeal(Long appealId, AppealHandleDTO dto) {
        UserAppeal appeal = appealMapper.selectById(appealId);
        if (appeal == null) {
            throw new BusinessException(404, "申诉不存在");
        }
        if (!Integer.valueOf(CommonConstant.APPEAL_PENDING).equals(appeal.getStatus())) {
            throw new BusinessException(400, "该申诉已处理");
        }
        User target = userMapper.selectById(appeal.getUserId());
        if (target == null) {
            throw new BusinessException(404, "申诉用户不存在");
        }
        Long adminId = StpUtil.getLoginIdAsLong();

        if (dto.getAction() == CommonConstant.APPEAL_APPROVED) {
            // 通过：解除全部处罚
            target.setStatus(CommonConstant.USER_STATUS_NORMAL);
            target.setBanFlags(CommonConstant.BAN_NONE);
            target.setBanExpireTime(null);
            target.setBanReason(null);
            userMapper.updateById(target);
            appeal.setStatus(CommonConstant.APPEAL_APPROVED);
            notificationService.create(target.getId(), "BAN", "申诉已通过",
                    "你的申诉已通过，账号处罚已全部解除，相关功能恢复正常使用。");
        } else if (dto.getAction() == CommonConstant.APPEAL_REJECTED) {
            // 驳回：维持原处罚
            appeal.setStatus(CommonConstant.APPEAL_REJECTED);
            notificationService.create(target.getId(), "BAN", "申诉被驳回",
                    "你的申诉未通过，原处罚维持不变。\n管理员回复：" + (dto.getAdminReply() != null ? dto.getAdminReply() : "无"));
        } else if (dto.getAction() == CommonConstant.APPEAL_REDUCED) {
            // 减轻处罚：替换为新的处罚配置
            if (dto.getBanFlags() == null || dto.getBanFlags() <= 0) {
                throw new BusinessException(400, "减轻处罚必须指定新的封禁功能");
            }
            if (dto.getBanDays() == null) {
                throw new BusinessException(400, "减轻处罚必须指定新的封禁天数");
            }
            target.setBanFlags(dto.getBanFlags());
            if (dto.getBanDays() >= 0) {
                target.setBanExpireTime(LocalDateTime.now().plusDays(dto.getBanDays()));
            } else {
                target.setBanExpireTime(null);
            }
            target.setBanReason(dto.getBanReason() != null ? dto.getBanReason() : "申诉减轻处罚");
            // 如果新处罚包含登录封禁，则同步 status
            if ((dto.getBanFlags() & CommonConstant.BAN_LOGIN) != 0) {
                target.setStatus(CommonConstant.USER_STATUS_DISABLED);
            } else {
                target.setStatus(CommonConstant.USER_STATUS_NORMAL);
            }
            userMapper.updateById(target);
            appeal.setStatus(CommonConstant.APPEAL_REDUCED);
            notificationService.create(target.getId(), "BAN", "申诉已处理（减轻处罚）",
                    "你的申诉已处理，处罚已减轻。\n管理员回复：" + (dto.getAdminReply() != null ? dto.getAdminReply() : "无"));
        }

        appeal.setAdminReply(dto.getAdminReply());
        appeal.setAdminId(adminId);
        appeal.setHandleTime(LocalDateTime.now());
        appealMapper.updateById(appeal);
    }

    private UserAppealVO toAppealVO(UserAppeal appeal) {
        UserAppealVO vo = new UserAppealVO();
        vo.setId(appeal.getId());
        vo.setUserId(appeal.getUserId());
        vo.setAppealType(appeal.getAppealType());
        vo.setAppealTypeText(switch (appeal.getAppealType() == null ? 3 : appeal.getAppealType()) {
            case 1 -> "评论封禁";
            case 2 -> "账号封禁";
            default -> "其他";
        });
        vo.setReason(appeal.getReason());
        vo.setStatus(appeal.getStatus());
        vo.setStatusText(switch (appeal.getStatus() == null ? 0 : appeal.getStatus()) {
            case 1 -> "已通过";
            case 2 -> "已驳回";
            case 3 -> "已减轻处罚";
            default -> "待处理";
        });
        vo.setAdminReply(appeal.getAdminReply());
        vo.setAdminId(appeal.getAdminId());
        vo.setHandleTime(appeal.getHandleTime());
        vo.setCreateTime(appeal.getCreateTime());
        User user = userMapper.selectById(appeal.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }
        if (appeal.getAdminId() != null) {
            User admin = userMapper.selectById(appeal.getAdminId());
            if (admin != null) {
                vo.setAdminName(admin.getNickname() != null ? admin.getNickname() : admin.getUsername());
            }
        }
        return vo;
    }

    // ==================== 工具方法 ====================

    private void fillAuditText(AuthorApplyVO vo) {
        vo.setAuditStatusText(switch (vo.getAuditStatus() == null ? 0 : vo.getAuditStatus()) {
            case 1 -> "已通过";
            case 2 -> "已驳回";
            default -> "待审核";
        });
    }

    private String bookStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "连载中";
            case 2 -> "已完结";
            case 3 -> "待审核";
            default -> "已下架";
        };
    }

    private void fillOrderText(OrderVO vo) {
        vo.setTypeText(CommonConstant.ORDER_TYPE_CHAPTER == vo.getType() ? "单章" : "整本");
        vo.setPayStatusText(switch (vo.getPayStatus() == null ? -1 : vo.getPayStatus()) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已关闭";
            case 3 -> "已退款";
            default -> "未知";
        });
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
