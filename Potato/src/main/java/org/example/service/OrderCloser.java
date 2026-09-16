package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.config.PotatoProperties;
import org.example.common.constant.CommonConstant;
import org.example.entity.Orders;
import org.example.mapper.OrdersMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单关闭服务：超时未支付订单自动关闭
 * 被定时任务与 RocketMQ 超时消费者共用，关闭逻辑保证幂等。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCloser {

    private final OrdersMapper ordersMapper;
    private final PotatoProperties potatoProperties;

    /**
     * 扫描并关闭所有已超时的待支付订单
     *
     * @return 本次关闭订单数
     */
    public int closeExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now()
                .minusMinutes(potatoProperties.getOrder().getTimeoutMinutes());
        List<Orders> pending = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getPayStatus, CommonConstant.ORDER_PAY_PENDING)
                .lt(Orders::getCreateTime, deadline));
        int count = 0;
        for (Orders order : pending) {
            if (doClose(order)) {
                count++;
            }
        }
        if (count > 0) {
            log.info("超时自动关闭订单 {} 笔", count);
        }
        return count;
    }

    /**
     * 按订单号关闭（RocketMQ 延迟消息触发），未超时或已支付不处理
     */
    public void closeIfPending(String orderNo) {
        Orders order = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getOrderNo, orderNo));
        if (order == null || !Integer.valueOf(CommonConstant.ORDER_PAY_PENDING).equals(order.getPayStatus())) {
            return;
        }
        LocalDateTime deadline = LocalDateTime.now()
                .minusMinutes(potatoProperties.getOrder().getTimeoutMinutes());
        if (order.getCreateTime().isBefore(deadline)) {
            doClose(order);
        }
    }

    private boolean doClose(Orders order) {
        if (!Integer.valueOf(CommonConstant.ORDER_PAY_PENDING).equals(order.getPayStatus())) {
            return false;
        }
        order.setPayStatus(CommonConstant.ORDER_PAY_CLOSED);
        order.setCloseTime(LocalDateTime.now());
        ordersMapper.updateById(order);
        return true;
    }
}
