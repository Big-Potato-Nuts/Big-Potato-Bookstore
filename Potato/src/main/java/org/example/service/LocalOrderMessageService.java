package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 本地模式订单消息服务（默认）：
 * 使用进程内异步线程完成支付后处理，开箱即用，无需外部中间件。
 * 配置 potato.mq.enabled=false（默认）时生效。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "potato.mq", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalOrderMessageService implements OrderMessageService {

    private final OrderPaidHandler orderPaidHandler;

    @Override
    @Async("potatoExecutor")
    public void publishOrderPaid(Long orderId) {
        log.info("[本地异步] 订单支付后处理开始, orderId={}", orderId);
        orderPaidHandler.handlePaidOrder(orderId);
    }

    @Override
    public void publishOrderTimeoutCheck(String orderNo) {
        // 本地模式：订单超时关闭由 OrderTimeoutTask 定时扫描兜底，此处无需处理
    }
}
