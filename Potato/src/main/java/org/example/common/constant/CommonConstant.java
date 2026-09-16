package org.example.common.constant;

/**
 * 全局业务常量
 */
public final class CommonConstant {

    private CommonConstant() {
    }

    /** 角色：读者 */
    public static final int ROLE_READER = 0;
    /** 角色：作者 */
    public static final int ROLE_AUTHOR = 1;
    /** 角色：管理员 */
    public static final int ROLE_ADMIN = 2;

    /** 用户状态：正常 */
    public static final int USER_STATUS_NORMAL = 1;
    /** 用户状态：禁用 */
    public static final int USER_STATUS_DISABLED = 0;

    // ==================== 封禁功能位掩码（ban_flags） ====================
    /** 未封禁 */
    public static final int BAN_NONE = 0;
    /** 禁止登录（仅永久封禁使用，用户完全无法登录） */
    public static final int BAN_LOGIN = 1;
    /** 封禁评论 */
    public static final int BAN_COMMENT = 2;
    /** 封禁点赞 */
    public static final int BAN_LIKE = 4;
    /** 封禁阅读/购买 */
    public static final int BAN_READ = 8;
    /** 作者警告（可上传书籍但新书待审核） */
    public static final int BAN_AUTHOR_WARNING = 16;
    /** 封禁写书（禁止上传/编辑书籍和章节） */
    public static final int BAN_AUTHOR_WRITE = 32;
    /** 全功能限制（可登录但所有功能操作均被禁止，用于有时限的账号封禁） */
    public static final int BAN_ALL_FUNCTIONS = 64;

    /** 封禁时长：永久（banDays = -1） */
    public static final int BAN_DAYS_FOREVER = -1;

    // ==================== 书籍状态 ====================
    /** 书籍：已下架 */
    public static final int BOOK_STATUS_OFF = 0;
    /** 书籍：连载中 */
    public static final int BOOK_STATUS_SERIALIZING = 1;
    /** 书籍：已完结 */
    public static final int BOOK_STATUS_FINISHED = 2;
    /** 书籍：待审核（作者警告期间上传的新书） */
    public static final int BOOK_STATUS_PENDING = 3;

    // ==================== 申诉状态 ====================
    /** 申诉：待处理 */
    public static final int APPEAL_PENDING = 0;
    /** 申诉：已通过（解除处罚） */
    public static final int APPEAL_APPROVED = 1;
    /** 申诉：已驳回 */
    public static final int APPEAL_REJECTED = 2;
    /** 申诉：已减轻处罚 */
    public static final int APPEAL_REDUCED = 3;

    /** 作者审核状态：待审核 */
    public static final int AUDIT_PENDING = 0;
    /** 作者审核状态：通过 */
    public static final int AUDIT_PASSED = 1;
    /** 作者审核状态：驳回 */
    public static final int AUDIT_REJECTED = 2;

    /** 章节免费 */
    public static final int CHAPTER_FREE = 1;
    /** 章节付费 */
    public static final int CHAPTER_PAID = 0;

    /** 订单类型：单章 */
    public static final int ORDER_TYPE_CHAPTER = 1;
    /** 订单类型：整本 */
    public static final int ORDER_TYPE_BOOK = 2;

    /** 订单支付状态：待支付 */
    public static final int ORDER_PAY_PENDING = 0;
    /** 订单支付状态：已支付 */
    public static final int ORDER_PAY_PAID = 1;
    /** 订单支付状态：已关闭 */
    public static final int ORDER_PAY_CLOSED = 2;
    /** 订单支付状态：已退款 */
    public static final int ORDER_PAY_REFUNDED = 3;

    /** 评论状态：正常 */
    public static final int COMMENT_NORMAL = 1;
    /** 评论状态：已屏蔽 */
    public static final int COMMENT_BLOCKED = 0;

    /** 书架：仅阅读记录 */
    public static final int SHELF_NOT_COLLECT = 0;
    /** 书架：已收藏 */
    public static final int SHELF_COLLECTED = 1;

    /** 余额不足业务码（前端用于提示充值） */
    public static final int CODE_BALANCE_NOT_ENOUGH = 3001;

    /** 已购买业务码 */
    public static final int CODE_ALREADY_PURCHASED = 3002;
}
