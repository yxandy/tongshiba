-- =============================================
-- 用户管理与权限系统 - 数据库初始化脚本
-- =============================================

-- 1. 创建单位表
CREATE TABLE forum_unit (
    unit_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '单位ID',
    unit_name VARCHAR(100) NOT NULL UNIQUE COMMENT '单位名称（原始名称）',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名称（总部等特殊名称）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛单位表';

-- 2. 创建部门表
CREATE TABLE forum_department (
    dept_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    unit_id BIGINT NOT NULL COMMENT '所属单位ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_unit_dept (unit_id, dept_name),
    FOREIGN KEY (unit_id) REFERENCES forum_unit(unit_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛部门表';

-- 3. 扩展用户表字段
ALTER TABLE forum_user 
ADD COLUMN role VARCHAR(20) DEFAULT 'user' COMMENT '角色: admin/sub_admin/user' AFTER is_admin,
ADD COLUMN is_rate_limited CHAR(1) DEFAULT '0' COMMENT '是否限流: 0-否, 1-是' AFTER role;

-- 4. 扩展评论表字段
ALTER TABLE forum_comment 
ADD COLUMN is_rate_limited CHAR(1) DEFAULT '0' COMMENT '是否限流评论: 0-否, 1-是' AFTER content;

-- 5. 创建索引
CREATE INDEX idx_forum_user_role ON forum_user(role);
CREATE INDEX idx_forum_user_rate_limited ON forum_user(is_rate_limited);
CREATE INDEX idx_forum_comment_rate_limited ON forum_comment(is_rate_limited);

-- =============================================
-- 初始化数据（可选）
-- =============================================

-- 初始化山东高速总部
INSERT INTO forum_unit (unit_name, display_name) 
VALUES ('山东高速股份有限公司', '总部')
ON DUPLICATE KEY UPDATE display_name = '总部';
