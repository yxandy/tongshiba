-- =========================================
-- 论坛帖子表字段扩展
-- 日期: 2025-12-25
-- =========================================

-- 1. 添加单位/部门字段（记录发帖时的用户信息）
ALTER TABLE forum_post 
ADD COLUMN user_unit VARCHAR(100) DEFAULT NULL COMMENT '发帖时用户单位' AFTER user_id,
ADD COLUMN user_dept VARCHAR(100) DEFAULT NULL COMMENT '发帖时用户部门' AFTER user_unit;

-- 2. 添加置顶相关字段
ALTER TABLE forum_post 
ADD COLUMN is_pinned CHAR(1) DEFAULT '0' COMMENT '是否置顶（0否 1是）',
ADD COLUMN pin_expire_time DATETIME DEFAULT NULL COMMENT '置顶过期时间（NULL表示永久）';

-- 3. 添加最后回复时间字段（用于排序）
ALTER TABLE forum_post 
ADD COLUMN last_reply_time DATETIME DEFAULT NULL COMMENT '最后回复时间';
