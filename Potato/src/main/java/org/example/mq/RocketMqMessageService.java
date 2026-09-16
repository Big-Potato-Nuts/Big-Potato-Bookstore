package org.example.mq;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.example.common.config.PotatoProperties;
import org.example.service.OrderMessageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * RocketMQ 订单消息生产者（potato.mq.enabled=true 时启用）
 *
 * - 支付成功：发送 orderPaidTopic（tag=PAID），消费端完成销量统计与站内通知；
 * - 下单：发送 orderTimeoutTopic（tag=TIMEOUT）延迟消息，到期后消费端检查并关闭超时订单。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "potato.mq", name = "enabled", havingValue = "true")
public class RocketMqMessageService implements OrderMessageService {

    public static final String TAG_PAID = "PAID";
    public static final String TAG_TIMEOUT = "TIMEOUT";

    private final PotatoProperties potatoProperties;

    private DefaultMQProducer producer;

    @PostConstruct
    public void init() {
        PotatoProperties.Rocketmq rocketmq = potatoProperties.getRocketmq();
        producer = new DefaultMQProducer(rocketmq.getProducerGroup());
        producer.setNamesrvAddr(rocketmq.getNameServer());
        producer.setRetryTimesWhenSendFailed(2);
        try {
            producer.start();
            log.info("RocketMQ 生产者启动成功, namesrv={}", rocketmq.getNameServer());
        } catch (Exception e) {
            log.error("RocketMQ 生产者启动失败，请检查 RocketMQ 服务", e);
            throw new IllegalStateException("RocketMQ 生产者启动失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (producer != null) {
            producer.shutdown();
        }
    }

    @Override
    public void publishOrderPaid(Long orderId) {
        Message message = new Message(
                potatoProperties.getRocketmq().getOrderPaidTopic(),
                TAG_PAID,
                String.valueOf(orderId).getBytes(StandardCharsets.UTF_8));
        send(message);
    }

    @Override
    public void publishOrderTimeoutCheck(String orderNo) {
        Message message = new Message(
                potatoProperties.getRocketmq().getOrderTimeoutTopic(),
                TAG_TIMEOUT,
                orderNo.getBytes(StandardCharsets.UTF_8));
        // 延迟消息：到期后进入消费端，触发超时关单
        message.setDelayTimeLevel(potatoProperties.getRocketmq().getTimeoutDelayLevel());
        send(message);
    }

    private void send(Message message) {
        try {
            producer.send(message);
        } catch (Exception e) {
            log.error("RocketMQ 消息发送失败, topic={}, tag={}", message.getTopic(), message.getTags(), e);
        }
    }
}
