-- 升级脚本：用户申诉系统
-- 新增 user_appeal 表，支持被封禁用户发起申诉，管理员审核处理

DROP TABLE IF EXISTS `user_appeal`;
CREATE TABLE `user_appeal` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '申诉ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '申诉用户ID',
  `appeal_type` TINYINT NOT NULL DEFAULT 3 COMMENT '申诉类型:1评论封禁/2账号封禁/3其他',
  `reason` VARCHAR(500) NOT NULL COMMENT '申诉理由',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0待处理/1已通过(解除处罚)/2已驳回/3已减轻处罚',
  `admin_reply` VARCHAR(500) DEFAULT NULL COMMENT '管理员回复',
  `admin_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理管理员ID',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申诉时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户申诉表';
