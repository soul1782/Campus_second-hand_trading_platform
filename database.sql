-- ============================================================
-- 校园二手交易平台数据库
-- Campus Second-hand Trading Platform Database
-- 创建时间: 2026-05-09
-- ============================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS campus_trade 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE campus_trade;

-- ============================================================
-- 2. 身份验证模块
-- ============================================================

-- 师生白名单（由学校教务处/人事处导入）
CREATE TABLE whitelist (
    whitelist_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) UNIQUE NOT NULL COMMENT '学号/工号',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    identity_type TINYINT NOT NULL COMMENT '1-学生 2-教职工',
    campus VARCHAR(100) NOT NULL COMMENT '所属校区',
    department VARCHAR(100) COMMENT '院系/部门',
    status TINYINT DEFAULT 1 COMMENT '0-已注销/离校 1-有效',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_campus (campus),
    INDEX idx_identity_type (identity_type)
) ENGINE=InnoDB COMMENT='师生白名单表';

-- 用户表（支持学生/教职工两种身份）
CREATE TABLE users (
    user_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) UNIQUE COMMENT '学号/工号',
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    real_name VARCHAR(50) COMMENT '真实姓名',

    identity_type TINYINT NOT NULL DEFAULT 1 COMMENT '1-学生 2-教职工',
    campus VARCHAR(100) NOT NULL COMMENT '所属校区',
    department VARCHAR(100) COMMENT '院系/部门',
    verify_status TINYINT DEFAULT 0 COMMENT '0-未认证 1-已认证 2-认证失败',
    verified_at TIMESTAMP NULL,

    status TINYINT DEFAULT 1 COMMENT '0-禁用 1-正常 2-锁定',
    last_login_at TIMESTAMP NULL,
    last_login_ip VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_student_id (student_id),
    INDEX idx_campus (campus),
    INDEX idx_verify_status (verify_status),
    INDEX idx_identity_type (identity_type)
) ENGINE=InnoDB COMMENT='用户表';

-- 密码重置/找回记录
CREATE TABLE password_resets (
    reset_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_token (token)
) ENGINE=InnoDB COMMENT='密码重置表';

-- ============================================================
-- 3. 物品管理模块
-- ============================================================

-- 物品分类（支持多级）
CREATE TABLE categories (
    category_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    parent_id INT UNSIGNED DEFAULT 0 COMMENT '父分类ID，0为顶级',
    category_name VARCHAR(50) NOT NULL,
    icon_url VARCHAR(500),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '0-禁用 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB COMMENT='物品分类表';

-- 物品表
CREATE TABLE products (
    product_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT UNSIGNED NOT NULL COMMENT '卖家ID',
    category_id INT UNSIGNED NOT NULL,

    title VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2) COMMENT '原价',

    condition_level TINYINT NOT NULL COMMENT '新旧程度: 1-全新 2-几乎全新 3-轻微使用 4-明显使用 5-较旧',
    brand VARCHAR(100) COMMENT '品牌',
    quantity INT UNSIGNED DEFAULT 1,

    campus VARCHAR(100) NOT NULL COMMENT '交易校区',
    trade_location VARCHAR(255) COMMENT '交易地点',
    trade_method TINYINT DEFAULT 3 COMMENT '1-仅自提 2-仅快递 3-两者皆可',
    is_bargain TINYINT DEFAULT 1 COMMENT '0-不议价 1-可议价',

    status TINYINT DEFAULT 1 COMMENT '0-待审核 1-在售 2-已售出 3-已下架 4-审核不通过',
    view_count INT UNSIGNED DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_seller_status (seller_id, status),
    INDEX idx_category_status (category_id, status),
    INDEX idx_campus_status (campus, status),
    INDEX idx_price (price),
    FULLTEXT INDEX ft_title_desc (title, description)
) ENGINE=InnoDB COMMENT='物品表';

-- 物品图片（支持多图）
CREATE TABLE product_images (
    image_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT UNSIGNED NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT DEFAULT 0,
    is_cover TINYINT DEFAULT 0 COMMENT '1-封面图',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    INDEX idx_product_sort (product_id, sort_order)
) ENGINE=InnoDB COMMENT='物品图片表';

-- 用户收藏
CREATE TABLE user_favorites (
    favorite_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_product (user_id, product_id)
) ENGINE=InnoDB COMMENT='用户收藏表';

-- 浏览历史
CREATE TABLE browse_history (
    history_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    browsed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    INDEX idx_user_time (user_id, browsed_at)
) ENGINE=InnoDB COMMENT='浏览历史表';

-- ============================================================
-- 4. 交易流程模块
-- ============================================================

-- 订单表
CREATE TABLE orders (
    order_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) UNIQUE NOT NULL COMMENT '订单编号: CT+年月日+6位随机数',
    buyer_id BIGINT UNSIGNED NOT NULL,
    seller_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,

    quantity INT UNSIGNED DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL COMMENT '成交单价',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总额',

    receiver_name VARCHAR(50),
    receiver_phone VARCHAR(20),
    receiver_address VARCHAR(255),

    status TINYINT DEFAULT 0 COMMENT '0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-退款中',
    remark VARCHAR(500) COMMENT '订单备注',

    paid_at TIMESTAMP NULL,
    shipped_at TIMESTAMP NULL,
    received_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_buyer_status (buyer_id, status),
    INDEX idx_seller_status (seller_id, status),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB COMMENT='订单表';

-- ============================================================
-- 5. 支付结算模块
-- ============================================================

-- 用户钱包/余额（资金托管用）
CREATE TABLE user_wallets (
    wallet_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED UNIQUE NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '可用余额',
    frozen_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结金额（交易中）',
    total_income DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计收入',
    total_expenditure DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计支出',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='用户钱包表';

-- 支付流水（微信支付 + 余额支付）
CREATE TABLE transactions (
    transaction_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    transaction_no VARCHAR(64) UNIQUE NOT NULL COMMENT '平台流水号',

    order_id BIGINT UNSIGNED NOT NULL,
    payer_id BIGINT UNSIGNED NOT NULL COMMENT '付款人',
    payee_id BIGINT UNSIGNED NOT NULL COMMENT '收款人',

    amount DECIMAL(10,2) NOT NULL,
    transaction_type TINYINT NOT NULL COMMENT '1-微信支付 2-余额支付 3-退款 4-平台结算',

    wx_prepay_id VARCHAR(64) COMMENT '微信预支付ID',
    wx_transaction_id VARCHAR(64) COMMENT '微信支付流水号',

    status TINYINT DEFAULT 0 COMMENT '0-待支付 1-支付成功 2-支付失败 3-已退款',
    paid_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (payer_id) REFERENCES users(user_id),
    FOREIGN KEY (payee_id) REFERENCES users(user_id),
    INDEX idx_order (order_id),
    INDEX idx_payer (payer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='支付流水表';

-- 退款/纠纷申诉表
CREATE TABLE disputes (
    dispute_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,

    initiator_id BIGINT UNSIGNED NOT NULL COMMENT '申诉发起者',
    respondent_id BIGINT UNSIGNED NOT NULL COMMENT '被申诉者',

    dispute_type TINYINT NOT NULL COMMENT '1-未收到货 2-货不对板 3-质量问题 4-其他',
    reason TEXT NOT NULL,
    evidence_urls JSON COMMENT '证据图片URL',

    status TINYINT DEFAULT 0 COMMENT '0-待处理 1-处理中 2-已解决 3-已驳回',
    platform_result TEXT COMMENT '平台处理结果',
    refund_amount DECIMAL(10,2) DEFAULT 0.00,

    handler_id BIGINT UNSIGNED COMMENT '处理人（管理员）',
    resolved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (initiator_id) REFERENCES users(user_id),
    FOREIGN KEY (respondent_id) REFERENCES users(user_id),
    INDEX idx_order (order_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='纠纷申诉表';

-- ============================================================
-- 6. 信用评价模块
-- ============================================================

-- 评价表（双向评价）
CREATE TABLE reviews (
    review_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,

    reviewer_id BIGINT UNSIGNED NOT NULL COMMENT '评价人',
    reviewee_id BIGINT UNSIGNED NOT NULL COMMENT '被评价人',
    product_id BIGINT UNSIGNED NOT NULL,

    rating TINYINT NOT NULL COMMENT '综合评分 1-5星',
    desc_match TINYINT COMMENT '描述相符 1-5星',
    attitude TINYINT COMMENT '服务态度 1-5星',
    logistics TINYINT COMMENT '物流速度 1-5星',

    content VARCHAR(500),
    is_anonymous TINYINT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (reviewer_id) REFERENCES users(user_id),
    FOREIGN KEY (reviewee_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    UNIQUE KEY uk_order_reviewer (order_id, reviewer_id),
    INDEX idx_reviewee (reviewee_id)
) ENGINE=InnoDB COMMENT='评价表';

-- 信用积分记录
CREATE TABLE credit_logs (
    log_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,

    change_type TINYINT NOT NULL COMMENT '1-交易完成加分 2-好评加分 3-差评扣分 4-违规扣分',
    change_value INT NOT NULL COMMENT '变化值（正数为加分）',
    current_score INT NOT NULL COMMENT '变化后积分',
    related_order_id BIGINT UNSIGNED,
    remark VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (related_order_id) REFERENCES orders(order_id),
    INDEX idx_user_time (user_id, created_at)
) ENGINE=InnoDB COMMENT='信用积分记录表';

-- ============================================================
-- 7. 即时通讯模块
-- ============================================================

CREATE TABLE conversations (
    conversation_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT UNSIGNED,
    buyer_id BIGINT UNSIGNED NOT NULL,
    seller_id BIGINT UNSIGNED NOT NULL,
    last_message TEXT,
    last_message_at TIMESTAMP NULL,
    unread_count_buyer INT UNSIGNED DEFAULT 0,
    unread_count_seller INT UNSIGNED DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE SET NULL,
    FOREIGN KEY (buyer_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_conversation (product_id, buyer_id, seller_id)
) ENGINE=InnoDB COMMENT='会话表';

CREATE TABLE messages (
    message_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT UNSIGNED NOT NULL,
    sender_id BIGINT UNSIGNED NOT NULL,
    message_type TINYINT DEFAULT 1 COMMENT '1-文本 2-图片 3-物品卡片',
    content TEXT,
    extra_data JSON COMMENT '附加数据（如物品ID、图片URL）',
    is_read TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id),
    INDEX idx_conversation_time (conversation_id, created_at)
) ENGINE=InnoDB COMMENT='消息表';

-- ============================================================
-- 8. 后台管理
-- ============================================================

CREATE TABLE admins (
    admin_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role TINYINT DEFAULT 1 COMMENT '1-运营 2-审核员 9-超级管理员',
    status TINYINT DEFAULT 1,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='管理员表';

CREATE TABLE system_logs (
    log_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    admin_id INT UNSIGNED,
    user_id BIGINT UNSIGNED,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT UNSIGNED,
    detail JSON,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin (admin_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB COMMENT='系统日志表';

-- ============================================================
-- 9. 初始化数据
-- ============================================================

-- 插入分类数据
INSERT INTO categories (category_name, parent_id, sort_order) VALUES
('数码电子', 0, 1),
('教材书籍', 0, 2),
('生活用品', 0, 3),
('交通工具', 0, 4),
('美妆护肤', 0, 5),
('服装鞋帽', 0, 6),
('其他闲置', 0, 7);

INSERT INTO categories (category_name, parent_id, sort_order) VALUES
('手机', 1, 1), ('电脑', 1, 2), ('平板', 1, 3), ('配件', 1, 4),
('教材', 2, 1), ('考研资料', 2, 2), ('课外书', 2, 3),
('家具', 3, 1), ('电器', 3, 2), ('日用品', 3, 3),
('自行车', 4, 1), ('电动车', 4, 2);

-- 插入白名单数据（测试用）
INSERT INTO whitelist (student_id, real_name, identity_type, campus, department, status) VALUES
('2024001', '张三', 1, '主校区', '计算机学院', 1),
('2024002', '李四', 1, '主校区', '数学学院', 1),
('T001', '王五', 2, '主校区', '人事处', 1);

-- 插入管理员（默认密码: admin123，生产环境请修改）
INSERT INTO admins (username, password_hash, role) VALUES
('admin', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 9);