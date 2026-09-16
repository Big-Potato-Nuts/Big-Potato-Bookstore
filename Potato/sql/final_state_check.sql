-- 最终数据状态整理：恢复种子申诉为待处理（供管理端演示）
SET NAMES utf8mb4;
UPDATE user_appeal SET status = 0, admin_reply = NULL, handle_time = NULL WHERE id = 100;

-- 数据快照
SELECT 'user' AS tbl, COUNT(*) AS cnt FROM `user`
UNION ALL SELECT 'author', COUNT(*) FROM author
UNION ALL SELECT 'book', COUNT(*) FROM book
UNION ALL SELECT 'chapter', COUNT(*) FROM chapter
UNION ALL SELECT 'bookshelf', COUNT(*) FROM bookshelf
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'comment', COUNT(*) FROM comment
UNION ALL SELECT 'user_appeal', COUNT(*) FROM user_appeal
UNION ALL SELECT 'notification', COUNT(*) FROM notification;

-- 各分类书籍数
SELECT c.id, c.name, COUNT(b.id) AS books FROM category c LEFT JOIN book b ON b.category_id = c.id GROUP BY c.id, c.name ORDER BY c.id;

-- 书籍状态分布
SELECT status, COUNT(*) AS cnt FROM book GROUP BY status;

-- 封禁中的用户
SELECT id, username, ban_flags, ban_reason, ban_expire_time FROM `user` WHERE ban_flags > 0 OR status = 0;
