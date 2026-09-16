package org.example.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 业务自定义配置（potato.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "potato")
public class PotatoProperties {

    private Order order = new Order();
    private Mq mq = new Mq();
    private Rocketmq rocketmq = new Rocketmq();

    @Data
    public static class Order {
        /** 未支付订单超时时间（分钟） */
        private int timeoutMinutes = 15;
    }

    @Data
    public static class Mq {
        /** 是否启用 RocketMQ */
        private boolean enabled = false;
    }

    @Data
    public static class Rocketmq {
        private String nameServer = "127.0.0.1:9876";
        private String producerGroup = "potato-producer-group";
        private String consumerGroup = "potato-consumer-group";
        private String orderPaidTopic = "potato-order-paid";
        private String orderTimeoutTopic = "potato-order-timeout";
        /** 订单超时延迟消息级别 */
        private int timeoutDelayLevel = 14;
    }
}
