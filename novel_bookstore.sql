-- 在线小说书城系统建表脚本
-- 使用前请先自行创建数据库（如 CREATE DATABASE novel DEFAULT CHARSET utf8mb4;）
-- 然后在客户端中选中该库后执行本脚本，或命令行: mysql -u root -p 库名 < novel_bookstore.sql

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt密文)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色:0读者/1作者/2管理员',
  `balance` INT NOT NULL DEFAULT 0 COMMENT '余额(单位:分)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1正常/0禁用',
  `ban_flags` INT NOT NULL DEFAULT 0 COMMENT '封禁功能位掩码: bit0(1)=禁止登录, bit1(2)=评论, bit2(4)=点赞, bit3(8)=阅读购买, bit4(16)=作者警告, bit5(32)=封禁写书, bit6(64)=全功能限制(可登录)',
  `ban_expire_time` DATETIME DEFAULT NULL COMMENT '封禁到期时间(NULL=永久)',
  `ban_reason` VARCHAR(255) DEFAULT NULL COMMENT '封禁原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `author`;
CREATE TABLE `author` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '作者ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联user.id(一对一)',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名(实名认证)',
  `id_card` VARCHAR(20) NOT NULL COMMENT '身份证号(脱敏展示)',
  `intro` VARCHAR(500) DEFAULT NULL COMMENT '作者简介',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态:0待审核/1通过/2驳回',
  `audit_reason` VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作者表';

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(20) NOT NULL COMMENT '分类名(玄幻/都市/言情等)',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号(升序)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1启用/0停用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小说分类表';

DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '小说ID',
  `author_id` BIGINT UNSIGNED NOT NULL COMMENT '关联author.id',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '关联category.id',
  `title` VARCHAR(100) NOT NULL COMMENT '书名',
  `cover` VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
  `intro` TEXT COMMENT '简介',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1连载中/2已完结/0已下架',
  `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐:1是/0否',
  `click_count` INT NOT NULL DEFAULT 0 COMMENT '阅读量',
  `collect_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_title` (`title`),
  KEY `idx_is_recommend` (`is_recommend`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小说表';

DROP TABLE IF EXISTS `chapter`;
CREATE TABLE `chapter` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `book_id` BIGINT UNSIGNED NOT NULL COMMENT '关联book.id',
  `title` VARCHAR(100) NOT NULL COMMENT '章节标题',
  `content` MEDIUMTEXT NOT NULL COMMENT '章节正文(文本存储)',
  `is_free` TINYINT NOT NULL DEFAULT 1 COMMENT '是否免费:1免费/0付费',
  `price` INT NOT NULL DEFAULT 0 COMMENT '价格(单位:分)',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '章节排序号',
  `words` INT NOT NULL DEFAULT 0 COMMENT '正文字数',
  `read_count` INT NOT NULL DEFAULT 0 COMMENT '阅读次数',
  `pay_count` INT NOT NULL DEFAULT 0 COMMENT '付费阅读次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_book_sort` (`book_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节表';

DROP TABLE IF EXISTS `bookshelf`;
CREATE TABLE `bookshelf` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联user.id',
  `book_id` BIGINT UNSIGNED NOT NULL COMMENT '关联book.id',
  `read_chapter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '最近阅读章节(阅读进度)',
  `read_position` INT DEFAULT NULL COMMENT '章节内阅读位置(书签)',
  `is_collect` TINYINT NOT NULL DEFAULT 1 COMMENT '是否收藏:1收藏/0仅阅读记录',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_book` (`user_id`, `book_id`),
  KEY `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书架表';

DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(40) NOT NULL COMMENT '订单号',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联user.id(购买者)',
  `book_id` BIGINT UNSIGNED NOT NULL COMMENT '关联book.id',
  `chapter_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联chapter.id(单章购买时非空)',
  `type` TINYINT NOT NULL COMMENT '订单类型:1单章/2整本',
  `amount` INT NOT NULL COMMENT '订单金额(单位:分)',
  `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态:0待支付/1已支付/2已关闭/3已退款',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `close_time` DATETIME DEFAULT NULL COMMENT '关闭时间(超时自动关闭)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_pay_status` (`pay_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '关联user.id',
  `book_id` BIGINT UNSIGNED NOT NULL COMMENT '关联book.id',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1正常/0已屏蔽',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

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

-- 初始化数据（密码均为 123456）
INSERT INTO `category` (`name`, `sort`, `status`) VALUES
('玄幻', 1, 1), ('都市', 2, 1), ('言情', 3, 1), ('悬疑', 4, 1),
('科幻', 5, 1), ('历史', 6, 1), ('仙侠', 7, 1), ('军事', 8, 1);

INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `role`, `balance`, `status`) VALUES
(1, 'admin',    '$2b$10$SOEgXYHMOmNDJ4Pb6503d.onS2o5JOZKfAonpLRcXFBA.K3AwZO.a', '系统管理员', 2, 0,     1),
(2, 'reader',   '$2b$10$SOEgXYHMOmNDJ4Pb6503d.onS2o5JOZKfAonpLRcXFBA.K3AwZO.a', '测试读者',   0, 10000, 1),
(3, 'author01', '$2b$10$SOEgXYHMOmNDJ4Pb6503d.onS2o5JOZKfAonpLRcXFBA.K3AwZO.a', '星空写手',   1, 0,     1);

INSERT INTO `author` (`id`, `user_id`, `real_name`, `id_card`, `intro`, `audit_status`, `audit_time`) VALUES
(1, 3, '张三', '360101********1234', '热爱科幻创作的测试作者。', 1, NOW());

INSERT INTO `book` (`id`, `author_id`, `category_id`, `title`, `intro`, `status`, `is_recommend`) VALUES
(1, 1, 5, '星海征途', '人类深空舰队驶向未知星海，寻找失联的殖民船黎明号。（测试数据，正文自写）', 1, 1);

INSERT INTO `chapter` (`id`, `book_id`, `title`, `content`, `is_free`, `price`, `sort`, `words`) VALUES
(1, 1, '第一章 启程', '星港的灯光在舷窗外缩成一条细线。林舟系好安全带，看着仪表盘上黎明号三个字缓缓亮起。舰长通过广播宣布，本次航行将持续四百个标准日，目的地是猎户座悬臂边缘的K-7殖民星。舱内安静得只剩通风系统的嗡鸣，林舟摸了摸胸口那枚旧徽章——那是他父亲留下的，上面刻着一行小字：永远不要回头。', 1, 0, 1, 132),
(2, 1, '第二章 深空信号', '航行第37天，通讯组截获一段异常信号。信号来自黎明号最后一次报点的方位，却带着不属于人类舰船编码的杂音。林舟盯着频谱图上的锯齿，忽然发现它们有规律地重复着——像某种语言，又像某种求救。舰长命令全舰进入戒备状态，而林舟知道，他们距离那个方位还有整整六十天的航程。', 1, 0, 2, 140),
(3, 1, '第三章 星门危机', '第十一周，舰队抵达信号源附近，看到的却是一道扭曲的星门。星门边缘不断溢出蓝白色的能量波纹，而黎明号的识别信号正从门内传来——断断续续，仿佛被困在时间缝隙里。林舟提出了一个疯狂的计划：派一艘侦察艇穿过星门，带回黎明号的航行日志。舰长沉默了很久，最终只说了三个字：批准了。', 0, 50, 3, 151);

INSERT INTO `bookshelf` (`user_id`, `book_id`, `read_chapter_id`, `read_position`, `is_collect`) VALUES
(2, 1, 1, 0, 1);

INSERT INTO `comment` (`user_id`, `book_id`, `content`, `status`) VALUES
(2, 1, '开篇节奏紧凑，星门设定很有想象力，期待后续章节！（测试评论）', 1);
