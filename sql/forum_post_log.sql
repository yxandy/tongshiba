-- =========================================
-- 帖子操作日志表
-- 日期: 2025-12-25
-- 用途: 记录帖子的发布、删除、恢复、置顶、锁定等操作
-- =========================================

DROP TABLE IF EXISTS forum_post_log;
CREATE TABLE forum_post_log (
    log_id          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    post_id         BIGINT(20)    NOT NULL                 COMMENT '帖子ID',
    action          VARCHAR(20)   NOT NULL                 COMMENT '操作类型(create/delete/restore/pin/unpin/lock/unlock)',
    operator_id     BIGINT(20)    DEFAULT NULL             COMMENT '操作人ID',
    operator_name   VARCHAR(50)   DEFAULT NULL             COMMENT '操作人名称',
    description     VARCHAR(200)  DEFAULT NULL             COMMENT '操作描述',
    operate_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (log_id),
    KEY idx_post_id (post_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='帖子操作日志表';
