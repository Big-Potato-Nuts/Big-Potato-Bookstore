-- 清理自动化测试残留数据（不触碰演示种子数据）
-- 注意：测试注册用户前缀为 tester_ / qa_writer_，种子作者是 writer02/03/04，禁止用 writer_% 通配
SET NAMES utf8mb4;

-- 删除测试书籍及其章节/书架记录
DELETE c FROM chapter c JOIN book b ON c.book_id = b.id WHERE b.title LIKE '【测试】%';
DELETE s FROM bookshelf s JOIN book b ON s.book_id = b.id WHERE b.title LIKE '【测试】%';
DELETE FROM book WHERE title LIKE '【测试】%';

-- 删除测试申诉
DELETE FROM user_appeal WHERE reason LIKE '【测试】%';

-- 删除测试用户及其作者记录（仅测试前缀）
DELETE a FROM author a JOIN user u ON a.user_id = u.id WHERE u.username LIKE 'tester_%' OR u.username LIKE 'qa_writer_%';
DELETE FROM user WHERE username LIKE 'tester_%' OR username LIKE 'qa_writer_%';

-- 校验
SELECT COUNT(*) AS remain_test_books FROM book WHERE title LIKE '【测试】%';
SELECT COUNT(*) AS remain_test_users FROM user WHERE username LIKE 'tester_%' OR username LIKE 'qa_writer_%';
