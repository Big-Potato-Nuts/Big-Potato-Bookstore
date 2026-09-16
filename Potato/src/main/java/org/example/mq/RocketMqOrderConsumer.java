package org.example.mq;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.example.common.config.PotatoProperties;
import org.example.service.OrderCloser;
import org.example.service.OrderPaidHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * RocketMQ 订单消息消费者（potato.mq.enabled=true 时启用）
 *  - PAID：执行订单支付后处理（销量统计、站内通知）
 *  - TIMEOUT：检查并关闭超时未支付订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "potato.mq", name = "enabled", havingValue = "true")
public class RocketMqOrderConsumer {

    private final PotatoProperties potatoProperties;
    private final OrderPaidHandler orderPaidHandler;
    private final OrderCloser orderCloser;

    private DefaultMQPushConsumer consumer;

    @PostConstruct
    public void init() {
        PotatoProperties.Rocketmq rocketmq = potatoProperties.getRocketmq();
        try {
            consumer = new DefaultMQPushConsumer(rocketmq.getConsumerGroup());
            consumer.setNamesrvAddr(rocketmq.getNameServer());
            consumer.subscribe(rocketmq.getOrderPaidTopic(), RocketMqMessageService.TAG_PAID);
            consumer.subscribe(rocketmq.getOrderTimeoutTopic(), RocketMqMessageService.TAG_TIMEOUT);
            consumer.setMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        try {
                            handle(msg);
                        } catch (Exception e) {
                            log.error("RocketMQ 消息消费失败, topic={}, msgId={}", msg.getTopic(), msg.getMsgId(), e);
                            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                        }
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            log.info("RocketMQ 消费者启动成功, topics={},{}", rocketmq.getOrderPaidTopic(), rocketmq.getOrderTimeoutTopic());
        } catch (Exception e) {
            log.error("RocketMQ 消费者启动失败，请检查 RocketMQ 服务", e);
            throw new IllegalStateException("RocketMQ 消费者启动失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (consumer != null) {
            consumer.shutdown();
        }
    }

    private void handle(MessageExt msg) {
        String body = new String(msg.getBody(), StandardCharsets.UTF_8);
        if (RocketMqMessageService.TAG_PAID.equals(msg.getTags())) {
            Long orderId = Long.valueOf(body);
            log.info("[RocketMQ] 收到订单支付成功消息, orderId={}", orderId);
            orderPaidHandler.handlePaidOrder(orderId);
        } else if (RocketMqMessageService.TAG_TIMEOUT.equals(msg.getTags())) {
            log.info("[RocketMQ] 收到订单超时检查消息, orderNo={}", body);
            orderCloser.closeIfPending(body);
        }
    }
}
