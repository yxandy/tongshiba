-- =========================================
-- 帖子表添加视频链接字段
-- 日期: 2025-12-26
-- =========================================

ALTER TABLE forum_post 
ADD COLUMN video_url VARCHAR(500) DEFAULT NULL COMMENT '视频链接' AFTER images;
