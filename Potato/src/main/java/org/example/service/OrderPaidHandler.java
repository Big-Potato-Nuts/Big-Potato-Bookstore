package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.constant.CommonConstant;
import org.example.entity.Book;
import org.example.entity.Chapter;
import org.example.entity.Orders;
import org.example.mapper.BookMapper;
import org.example.mapper.ChapterMapper;
import org.example.mapper.OrdersMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单支付成功后的统一处理逻辑：
 * ① 销量统计（章节付费次数累加）；② 站内通知生成。
 * 本地异步线程与 RocketMQ 消费者共用本组件，保证处理逻辑一致。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidHandler {

    private final OrdersMapper ordersMapper;
    private final ChapterMapper chapterMapper;
    private final BookMapper bookMapper;
    private final NotificationService notificationService;

    /**
     * 处理已支付订单（幂等：仅处理 payStatus=1 的订单）
     */
    public void handlePaidOrder(Long orderId) {
        try {
            Orders order = ordersMapper.selectById(orderId);
            if (order == null || !Integer.valueOf(CommonConstant.ORDER_PAY_PAID).equals(order.getPayStatus())) {
                return;
            }

            // ① 销量统计：单章购买累加该章节 pay_count；整本购买累加该书全部付费章节
            if (CommonConstant.ORDER_TYPE_CHAPTER == order.getType() && order.getChapterId() != null) {
                Chapter chapter = chapterMapper.selectById(order.getChapterId());
                if (chapter != null) {
                    chapter.setPayCount(chapter.getPayCount() + 1);
                    chapterMapper.updateById(chapter);
                }
            } else if (CommonConstant.ORDER_TYPE_BOOK == order.getType()) {
                List<Chapter> paidChapters = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                        .eq(Chapter::getBookId, order.getBookId())
                        .eq(Chapter::getIsFree, CommonConstant.CHAPTER_PAID));
                for (Chapter chapter : paidChapters) {
                    chapter.setPayCount(chapter.getPayCount() + 1);
                    chapterMapper.updateById(chapter);
                }
            }

            // ② 站内通知
            Book book = bookMapper.selectById(order.getBookId());
            String bookTitle = book != null ? book.getTitle() : "未知小说";
            String content;
            if (CommonConstant.ORDER_TYPE_CHAPTER == order.getType() && order.getChapterId() != null) {
                Chapter chapter = chapterMapper.selectById(order.getChapterId());
                String chapterTitle = chapter != null ? chapter.getTitle() : "未知章节";
                content = "你已成功购买《" + bookTitle + "》章节「" + chapterTitle + "」，快去阅读吧！";
            } else {
                content = "你已成功购买《" + bookTitle + "》整本小说，快去阅读吧！";
            }
            notificationService.create(order.getUserId(), "PURCHASE", "购买成功", content);
        } catch (Exception e) {
            log.error("订单支付后处理失败, orderId={}", orderId, e);
        }
    }
}
