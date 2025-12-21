-- =============================================
-- 关注帖子功能数据库变更
-- =============================================

-- 1. 新增帖子关注表
CREATE TABLE IF NOT EXISTS forum_post_follow (
    follow_id    BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '关注ID',
    user_id      BIGINT(20)  NOT NULL               COMMENT '用户ID',
    post_id      BIGINT(20)  NOT NULL               COMMENT '帖子ID',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (follow_id),
    UNIQUE KEY idx_user_post (user_id, post_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='帖子关注表';

-- 2. 帖子表新增最后回复时间字段 (用于排序)
ALTER TABLE forum_post ADD COLUMN IF NOT EXISTS last_reply_time DATETIME DEFAULT NULL COMMENT '最后回复时间';

-- 3. 更新现有帖子的 last_reply_time (取最新评论时间，如果没有评论则用帖子创建时间)
UPDATE forum_post p 
SET p.last_reply_time = COALESCE(
    (SELECT MAX(c.create_time) FROM forum_comment c WHERE c.post_id = p.post_id AND c.del_flag = '0'),
    p.create_time
);
