-- =========================================
-- 评论删除日志表
-- 日期: 2025-12-25
-- 用途: 记录评论的删除操作
-- =========================================

DROP TABLE IF EXISTS forum_comment_log;
CREATE TABLE forum_comment_log (
    log_id          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    post_id         BIGINT(20)    NOT NULL                 COMMENT '帖子ID',
    comment_id      BIGINT(20)    NOT NULL                 COMMENT '评论ID',
    floor_num       INT(11)       DEFAULT NULL             COMMENT '楼层号',
    content_summary VARCHAR(100)  DEFAULT NULL             COMMENT '评论内容摘要',
    operator_name   VARCHAR(50)   DEFAULT NULL             COMMENT '删除人名称',
    operate_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (log_id),
    KEY idx_post_id (post_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='评论删除日志表';
