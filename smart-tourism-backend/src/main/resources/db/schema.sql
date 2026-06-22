-- 智慧旅游推荐导览平台数据库表结构
-- 适配现有项目实体类

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS smart_tourism DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_tourism;

-- 2. 用户表 (sys_user)
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(60) NOT NULL COMMENT '密码（BCrypt加密）',
    `password_plain` VARCHAR(50) DEFAULT NULL COMMENT '明文密码（仅用于开发测试）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试用户（密码：123456，已BCrypt加密）
INSERT INTO `sys_user` (`username`, `password`, `password_plain`, `nickname`, `role`, `avatar`, `status`) VALUES 
('admin', '$2a$10$x9KEy8LRg7WqntM06b8sB.108IB8sopSc0Yf26gR43c0/Qq.HhvfO', '123456', '系统管理员', 'ADMIN', '/images/avatar/admin.jpg', 1),
('xiaoming', '$2a$10$x9KEy8LRg7WqntM06b8sB.108IB8sopSc0Yf26gR43c0/Qq.HhvfO', '123456', '旅行达人小明', 'USER', '/images/avatar/user1.jpg', 1);

-- 3. 景点表 (scenic_spot)
CREATE TABLE `scenic_spot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '景点名称',
    `description` TEXT COMMENT '景点简介',
    `images` TEXT COMMENT '图片地址JSON数组',
    `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `ticket_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '门票价格',
    `opening_hours` VARCHAR(100) DEFAULT NULL COMMENT '开放时间',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签JSON数组',
    `rating` DECIMAL(2,1) DEFAULT 4.5 COMMENT '评分',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_rating` (`rating`),
    INDEX `idx_view_count` (`view_count`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点信息表';

-- 插入测试景点数据
INSERT INTO `scenic_spot` (`name`, `description`, `latitude`, `longitude`, `ticket_price`, `opening_hours`, `images`, `rating`, `tags`, `view_count`) VALUES 
('北京故宫博物院', '中国明清两代的皇家宫殿，旧称为紫禁城。', 39.916345, 116.397155, 60.00, '08:30-17:00', '["/images/scenic/gugong.jpg"]', 4.9, '["历史","文化"]', 10000),
('杭州西湖风景区', '西湖，位于浙江省杭州市西湖区龙井路1号。', 30.242865, 120.148681, 0.00, '全天开放', '["/images/scenic/xihu.jpg"]', 4.8, '["自然","免费"]', 8500),
('上海迪士尼乐园', '中国内地首座迪士尼主题乐园。', 31.143378, 121.669064, 399.00, '09:00-21:00', '["/images/scenic/disney.jpg"]', 4.7, '["主题乐园"]', 12000);

-- 4. 酒店表 (hotel)
CREATE TABLE `hotel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '酒店名称',
    `description` TEXT COMMENT '酒店描述',
    `images` TEXT COMMENT '图片地址JSON数组',
    `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `price_min` DECIMAL(10,2) DEFAULT NULL COMMENT '最低价格',
    `price_max` DECIMAL(10,2) DEFAULT NULL COMMENT '最高价格',
    `star_rating` TINYINT DEFAULT 3 COMMENT '星级：1-5',
    `amenities` TEXT COMMENT '设施标签JSON数组',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_star_rating` (`star_rating`),
    INDEX `idx_price` (`price_min`, `price_max`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店表';

-- 插入测试酒店数据
INSERT INTO `hotel` (`name`, `description`, `address`, `latitude`, `longitude`, `price_min`, `price_max`, `star_rating`, `images`, `amenities`) VALUES 
('北京王府井大饭店', '位于北京市中心的五星级酒店', '北京市东城区王府井大街', 39.914889, 116.410579, 688.00, 1888.00, 5, '["/images/hotel/bj_hotel.jpg"]', '["WiFi","停车场","健身房","游泳池"]'),
('杭州西湖国宾馆', '坐落于西湖畔的豪华酒店', '浙江省杭州市西湖区', 30.251281, 120.130203, 1288.00, 3888.00, 5, '["/images/hotel/hz_hotel.jpg"]', '["WiFi","停车场","SPA","餐厅"]');

-- 5. 旅游攻略表 (travel_guide)
CREATE TABLE `travel_guide` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `content` LONGTEXT COMMENT '富文本内容',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `collect_count` INT DEFAULT 0 COMMENT '收藏数',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已发布 2-已拒绝',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅游攻略表';

-- 插入测试攻略数据
INSERT INTO `travel_guide` (`user_id`, `title`, `content`, `cover_image`, `view_count`, `like_count`, `collect_count`, `status`) VALUES 
(2, '北京三日游完美攻略', '# 第一天\n去故宫参观...\n# 第二天\n去长城...', '/images/guide/beijing.jpg', 8564, 1256, 520, 1);

-- 6. 订单表 (orders)
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_type` VARCHAR(20) NOT NULL COMMENT '商品类型：SCENIC/HOTEL',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/PAID/COMPLETED/CANCELLED',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 7. 聊天消息表 (chat_message)
CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `message_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT/IMAGE',
    `read_status` TINYINT DEFAULT 0 COMMENT '已读状态：0-未读 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_sender` (`sender_id`),
    INDEX `idx_receiver` (`receiver_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 8. 用户行为表 (interactions) - 用于推荐系统
CREATE TABLE `interactions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型：SCENIC/HOTEL/GUIDE',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `action_type` VARCHAR(20) NOT NULL COMMENT '行为类型：VIEW/FAVORITE/LIKE',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_action` (`action_type`),
    UNIQUE KEY `uk_user_target_action` (`user_id`, `target_type`, `target_id`, `action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';

-- 9. 轮播图表 (banner)
CREATE TABLE `banner` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
    `link_url` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_sort` (`sort`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 10. 公告表 (notice)
CREATE TABLE `notice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `type` VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '类型：SYSTEM-系统公告 ACTIVITY-活动公告',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已发布',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';
