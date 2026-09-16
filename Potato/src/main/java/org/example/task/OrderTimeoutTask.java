package org.example.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.OrderCloser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时自动关闭定时任务（默认兜底方案，每分钟扫描一次）
 * 若启用了 RocketMQ 延迟消息，二者并存时关闭逻辑幂等，不会重复关闭。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderCloser orderCloser;

    @Scheduled(fixedDelay = 60_000)
    public void closeTimeoutOrders() {
        orderCloser.closeExpiredOrders();
    }
}
