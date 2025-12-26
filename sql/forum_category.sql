-- ========================================
-- 论坛分类表
-- ========================================

CREATE TABLE IF NOT EXISTS forum_category (
  category_id   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
  name          VARCHAR(50) NOT NULL COMMENT '分类名称',
  sort_order    INT DEFAULT 0 COMMENT '排序顺序(越小越靠前)',
  status        CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛分类表';

-- ========================================
-- 修改帖子表，添加分类ID字段
-- ========================================

ALTER TABLE forum_post 
ADD COLUMN category_id BIGINT DEFAULT NULL COMMENT '分类ID' AFTER user_dept;

-- 添加外键索引（可选，视需求而定）
-- ALTER TABLE forum_post ADD INDEX idx_category_id (category_id);

-- ========================================
-- 插入默认分类数据
-- ========================================

INSERT INTO forum_category (name, sort_order, status) VALUES 
('综合交流', 1, '0'),
('工作求助', 2, '0'),
('经验分享', 3, '0'),
('吐槽灌水', 4, '0'),
('失物招领', 5, '0');
