-- ============================================================
-- 在线小说书城系统 · 增量升级脚本
-- 新增：站内通知表 notification（用于购买成功提醒、审核结果通知等）
-- 使用方式：在已导入 novel_bookstore.sql 的数据库中执行本脚本
-- 例如：mysql -u root -p Potato < upgrade_notification.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID(关联user.id)',
  `type` VARCHAR(20) NOT NULL DEFAULT 'SYSTEM' COMMENT '通知类型:SYSTEM/PURCHASE/AUDIT',
  `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读:1已读/0未读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知表';
