package org.example.service;

/**
 * 订单消息服务：支付成功后异步处理 + 订单超时检查
 * 本地模式（默认）：进程内异步线程处理；
 * RocketMQ 模式（potato.mq.enabled=true）：生产者 + 消费者解耦处理。
 */
public interface OrderMessageService {

    /**
     * 订单支付成功后的异步处理（销量统计、站内通知等）
     */
    void publishOrderPaid(Long orderId);

    /**
     * 创建待支付订单时发送超时检查消息
     * 本地模式为空操作（由定时任务兜底），RocketMQ 模式发送延迟消息。
     */
    void publishOrderTimeoutCheck(String orderNo);
}
