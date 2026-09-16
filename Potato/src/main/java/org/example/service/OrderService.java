package org.example.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CommonConstant;
import org.example.common.config.PotatoProperties;
import org.example.common.exception.BusinessException;
import org.example.common.result.PageResult;
import org.example.common.result.ResultCode;
import org.example.dto.OrderCreateDTO;
import org.example.dto.OrderPayDTO;
import org.example.entity.Book;
import org.example.entity.Chapter;
import org.example.entity.Orders;
import org.example.entity.User;
import org.example.mapper.BookMapper;
import org.example.mapper.ChapterMapper;
import org.example.mapper.OrdersMapper;
import org.example.mapper.UserMapper;
import org.example.vo.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单交易服务：模拟下单、模拟支付、订单查询
 * 业务口径：下单时校验余额 → 创建待支付订单 → 确认支付扣减余额并放行阅读权限。
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersMapper ordersMapper;
    private final BookMapper bookMapper;
    private final ChapterMapper chapterMapper;
    private final UserMapper userMapper;
    private final OrderMessageService orderMessageService;
    private final PotatoProperties potatoProperties;
    private final BanService banService;

    /** 创建订单（待支付） */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(OrderCreateDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 阅读购买封禁校验
        banService.checkBan(userId, CommonConstant.BAN_READ, "购买书籍");
        Book book = bookMapper.selectById(dto.getBookId());
        if (book == null || Integer.valueOf(CommonConstant.BOOK_STATUS_OFF).equals(book.getStatus())) {
            throw new BusinessException(404, "小说不存在或已下架");
        }

        Long chapterId = null;
        String chapterTitle = null;
        int amount;
        if (CommonConstant.ORDER_TYPE_CHAPTER == dto.getType()) {
            if (dto.getChapterId() == null) {
                throw new BusinessException(400, "单章购买必须指定章节");
            }
            Chapter chapter = chapterMapper.selectById(dto.getChapterId());
            if (chapter == null || !chapter.getBookId().equals(book.getId())) {
                throw new BusinessException(400, "章节不存在或不属于该小说");
            }
            if (Integer.valueOf(CommonConstant.CHAPTER_FREE).equals(chapter.getIsFree())) {
                throw new BusinessException(400, "免费章节无需购买");
            }
            if (hasPurchased(userId, book.getId(), chapter.getId())) {
                throw new BusinessException(CommonConstant.CODE_ALREADY_PURCHASED, "该章节已购买，无需重复购买");
            }
            if (chapter.getPrice() == null || chapter.getPrice() <= 0) {
                throw new BusinessException(400, "该章节价格异常");
            }
            chapterId = chapter.getId();
            chapterTitle = chapter.getTitle();
            amount = chapter.getPrice();
        } else if (CommonConstant.ORDER_TYPE_BOOK == dto.getType()) {
            if (hasPurchasedWholeBook(userId, book.getId())) {
                throw new BusinessException(CommonConstant.CODE_ALREADY_PURCHASED, "你已购买整本小说，无需重复购买");
            }
            amount = chapterMapper.selectPaidAmountByBook(book.getId());
            if (amount <= 0) {
                throw new BusinessException(400, "本书暂无付费章节，无需购买");
            }
        } else {
            throw new BusinessException(400, "订单类型不合法");
        }

        // 下单时校验余额（确认支付时再次校验并扣减）
        User user = userMapper.selectById(userId);
        if (user == null || user.getBalance() == null || user.getBalance() < amount) {
            throw new BusinessException(CommonConstant.CODE_BALANCE_NOT_ENOUGH, "余额不足，请先充值");
        }

        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setBookId(book.getId());
        order.setChapterId(chapterId);
        order.setType(dto.getType());
        order.setAmount(amount);
        order.setPayStatus(CommonConstant.ORDER_PAY_PENDING);
        ordersMapper.insert(order);

        // 超时检查消息（本地模式为空操作，RocketMQ 模式发送延迟消息）
        orderMessageService.publishOrderTimeoutCheck(order.getOrderNo());

        // 回读订单，拿到数据库默认值（createTime 等）
        Orders saved = ordersMapper.selectById(order.getId());
        return toVO(saved);
    }

    /** 确认支付（模拟支付：余额扣减 → 订单置为已支付 → 异步销量统计/站内通知） */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO pay(OrderPayDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderNo, dto.getOrderNo()));
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该订单");
        }
        // 幂等：已支付直接返回当前状态
        if (Integer.valueOf(CommonConstant.ORDER_PAY_PAID).equals(order.getPayStatus())) {
            return toVO(order);
        }
        if (Integer.valueOf(CommonConstant.ORDER_PAY_CLOSED).equals(order.getPayStatus())) {
            throw new BusinessException(400, "订单已关闭，请重新下单");
        }
        if (Integer.valueOf(CommonConstant.ORDER_PAY_REFUNDED).equals(order.getPayStatus())) {
            throw new BusinessException(400, "订单已退款");
        }
        // 超时检查
        long timeoutMinutes = potatoProperties.getOrder().getTimeoutMinutes();
        if (order.getCreateTime() == null
                || order.getCreateTime().isBefore(LocalDateTime.now().minusMinutes(timeoutMinutes))) {
            order.setPayStatus(CommonConstant.ORDER_PAY_CLOSED);
            order.setCloseTime(LocalDateTime.now());
            ordersMapper.updateById(order);
            throw new BusinessException(400, "订单已超时关闭，请重新下单");
        }
        // 扣减余额
        User user = userMapper.selectById(userId);
        if (user == null || user.getBalance() == null || user.getBalance() < order.getAmount()) {
            throw new BusinessException(CommonConstant.CODE_BALANCE_NOT_ENOUGH, "余额不足，请先充值");
        }
        user.setBalance(user.getBalance() - order.getAmount());
        userMapper.updateById(user);

        order.setPayStatus(CommonConstant.ORDER_PAY_PAID);
        order.setPayTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        // 事务提交后再触发异步处理（销量统计 + 站内通知），
        // 避免异步线程在事务未提交时读到旧状态（pay_status=0）导致跳过处理
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderMessageService.publishOrderPaid(order.getId());
            }
        });

        return toVO(order);
    }

    /** 我的订单 */
    public PageResult<OrderVO> myOrders(Long userId, int page, int size) {
        IPage<OrderVO> result = ordersMapper.selectOrderPage(new Page<>(page, size),
                null, userId, null, null, null);
        result.getRecords().forEach(this::fillText);
        return PageResult.of(result);
    }

    /** 订单详情（本人） */
    public OrderVO detail(Long userId, String orderNo) {
        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看该订单");
        }
        return toVO(order);
    }

    private boolean hasPurchased(Long userId, Long bookId, Long chapterId) {
        Long count = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getPayStatus, CommonConstant.ORDER_PAY_PAID)
                .and(w -> w.eq(Orders::getType, CommonConstant.ORDER_TYPE_CHAPTER)
                        .eq(Orders::getChapterId, chapterId)
                        .or()
                        .eq(Orders::getType, CommonConstant.ORDER_TYPE_BOOK)
                        .eq(Orders::getBookId, bookId)));
        return count > 0;
    }

    private boolean hasPurchasedWholeBook(Long userId, Long bookId) {
        Long count = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getBookId, bookId)
                .eq(Orders::getType, CommonConstant.ORDER_TYPE_BOOK)
                .eq(Orders::getPayStatus, CommonConstant.ORDER_PAY_PAID));
        return count > 0;
    }

    private OrderVO toVO(Orders order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
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
        vo.setBookId(order.getBookId());
        vo.setChapterId(order.getChapterId());
        vo.setType(order.getType());
        vo.setAmount(order.getAmount());
        vo.setPayStatus(order.getPayStatus());
        vo.setPayTime(order.getPayTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setCreateTime(order.getCreateTime());
        fillText(vo);
        return vo;
    }

    private void fillText(OrderVO vo) {
        vo.setTypeText(CommonConstant.ORDER_TYPE_CHAPTER == vo.getType() ? "单章" : "整本");
        vo.setPayStatusText(switch (vo.getPayStatus() == null ? -1 : vo.getPayStatus()) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已关闭";
            case 3 -> "已退款";
            default -> "未知";
        });
    }

    private String generateOrderNo() {
        return "NO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }
}
