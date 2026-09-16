-- 升级脚本：用户自定义封禁系统
-- 在 user 表中新增封禁相关字段，支持按功能封禁和定时封禁

ALTER TABLE `user`
  ADD COLUMN `ban_flags` INT NOT NULL DEFAULT 0 COMMENT '封禁功能位掩码: bit0(1)=登录/全功能, bit1(2)=评论, bit2(4)=点赞, bit3(8)=阅读购买' AFTER `status`,
  ADD COLUMN `ban_expire_time` DATETIME DEFAULT NULL COMMENT '封禁到期时间(NULL=永久)' AFTER `ban_flags`,
  ADD COLUMN `ban_reason` VARCHAR(255) DEFAULT NULL COMMENT '封禁原因' AFTER `ban_expire_time`;

-- 说明：
-- 1. status=0 仍表示账号被禁用（无法登录），等同于全功能永久封禁，兼容旧逻辑。
-- 2. status=1 但 ban_flags != 0 时，用户可登录但被封禁的功能不可用。
-- 3. ban_expire_time 不为 NULL 且早于当前时间时，封禁自动失效（登录时自动清除）。
-- 4. 位掩码可组合，例如 ban_flags=6 表示同时封禁评论(2)和点赞(4)。
