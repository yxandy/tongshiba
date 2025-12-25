-- =========================================
-- 论坛评论表字段扩展
-- 日期: 2025-12-25
-- =========================================

-- 1. 添加单位/部门字段（记录评论时的用户信息）
ALTER TABLE forum_comment 
ADD COLUMN user_unit VARCHAR(100) DEFAULT NULL COMMENT '评论时用户单位' AFTER user_id,
ADD COLUMN user_dept VARCHAR(100) DEFAULT NULL COMMENT '评论时用户部门' AFTER user_unit;

-- 2. 添加删除人字段（记录谁删除了评论）
ALTER TABLE forum_comment 
ADD COLUMN deleted_by BIGINT(20) DEFAULT NULL COMMENT '删除人用户ID';
