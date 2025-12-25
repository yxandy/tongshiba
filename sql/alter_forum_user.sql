-- =========================================
-- 论坛用户表字段扩展
-- 日期: 2025-12-25
-- =========================================

-- 1. 添加单位字段
ALTER TABLE forum_user 
ADD COLUMN unit VARCHAR(100) DEFAULT NULL COMMENT '单位名称' AFTER department;

-- 2. 添加管理员标识字段
ALTER TABLE forum_user 
ADD COLUMN is_admin CHAR(1) DEFAULT '0' COMMENT '是否管理员（0否 1是）';
