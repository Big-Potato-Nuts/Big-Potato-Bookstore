-- 恢复被清理脚本误删的种子作者（writer02/03/04 = user 17/18/19, author 5/6/7）
-- 同时清理测试残留（排除种子 writer02/03/04）
SET NAMES utf8mb4;

-- 1) 清理测试产生的 writer_xxx（排除种子账号）
DELETE a FROM author a JOIN user u ON a.user_id = u.id WHERE u.username LIKE 'writer_%' AND u.username NOT IN ('writer02','writer03','writer04');
DELETE FROM user WHERE username LIKE 'writer_%' AND username NOT IN ('writer02','writer03','writer04');

-- 2) 恢复种子作者用户
INSERT INTO `user` (id, username, password, nickname, role, balance, status, ban_flags, ban_reason, create_time) VALUES
(17, 'writer02', '$2b$10$SOEgXYHMOmNDJ4Pb6503d.onS2o5JOZKfAonpLRcXFBA.K3AwZO.a', '林间书客', 1, 0, 1, 0, NULL, NOW()),
(18, 'writer03', '$2b$10$SOEgXYHMOmNDJ4Pb6503d.onS2o5JOZKfAonpLRcXFBA.K3AwZO.a', '江畔听雨', 1, 0, 1, 0, NULL, NOW()),
(19, 'writer04', '$2b$10$SOEgXYHMOmNDJ4Pb6503d.onS2o5JOZKfAonpLRcXFBA.K3AwZO.a', '半盏清茶', 1, 0, 1, 0, NULL, NOW())
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role);

-- 3) 恢复种子作者记录
INSERT INTO `author` (id, user_id, real_name, id_card, intro, audit_status, audit_time, create_time) VALUES
(5, 17, '陈望舒', '360101199203154328', '擅长东方玄幻与古典言情，文风热血细腻兼具。', 1, NOW(), NOW()),
(6, 18, '江离',   '440101198807126541', '悬疑推理爱好者，专注反转与氛围营造。',     1, NOW(), NOW()),
(7, 19, '李清和', '510101199501238716', '都市与历史题材写手，细节考据控。',         1, NOW(), NOW())
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- 4) 删除测试书籍及其章节/书架记录
DELETE c FROM chapter c JOIN book b ON c.book_id = b.id WHERE b.title LIKE '【测试】%';
DELETE s FROM bookshelf s JOIN book b ON s.book_id = b.id WHERE b.title LIKE '【测试】%';
DELETE FROM book WHERE title LIKE '【测试】%';

-- 5) 删除测试申诉
DELETE FROM user_appeal WHERE reason LIKE '【测试】%';

-- 6) 清理 tester_% 测试用户
DELETE a FROM author a JOIN user u ON a.user_id = u.id WHERE u.username LIKE 'tester_%';
DELETE FROM user WHERE username LIKE 'tester_%';

-- 校验
SELECT id, username, role FROM user WHERE id IN (17,18,19);
SELECT id, user_id, real_name FROM author WHERE id IN (5,6,7);
SELECT COUNT(*) AS remain_test_books FROM book WHERE title LIKE '【测试】%';
