-- ----------------------------
-- 论坛用户表（企业微信用户同步）
-- ----------------------------
DROP TABLE IF EXISTS forum_user;
CREATE TABLE forum_user (
    user_id         BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    wx_userid       VARCHAR(100)  NOT NULL                 COMMENT '企业微信UserID',
    nickname        VARCHAR(50)   DEFAULT ''               COMMENT '用户昵称',
    avatar          VARCHAR(500)  DEFAULT ''               COMMENT '头像URL',
    department      VARCHAR(200)  DEFAULT ''               COMMENT '部门',
    status          CHAR(1)       DEFAULT '0'              COMMENT '状态（0正常 1禁言）',
    ban_end_time    DATETIME      DEFAULT NULL             COMMENT '禁言结束时间',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY idx_wx_userid (wx_userid)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='论坛用户表';

-- ----------------------------
-- 帖子表
-- ----------------------------
DROP TABLE IF EXISTS forum_post;
CREATE TABLE forum_post (
    post_id         BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '帖子ID',
    user_id         BIGINT(20)    NOT NULL                 COMMENT '发帖用户ID',
    title           VARCHAR(200)  NOT NULL                 COMMENT '帖子标题',
    content         TEXT                                   COMMENT '帖子内容',
    images          LONGTEXT                                 COMMENT '图片URL列表(JSON数组，支持Base64)',
    view_count      INT(11)       DEFAULT 0                COMMENT '浏览次数',
    comment_count   INT(11)       DEFAULT 0                COMMENT '评论数量',
    is_locked       CHAR(1)       DEFAULT '0'              COMMENT '是否锁定（0否 1是）',
    del_flag        CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 1删除）',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '发帖时间',
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (post_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='帖子表';

-- ----------------------------
-- 评论表
-- ----------------------------
DROP TABLE IF EXISTS forum_comment;
CREATE TABLE forum_comment (
    comment_id      BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '评论ID',
    post_id         BIGINT(20)    NOT NULL                 COMMENT '帖子ID',
    user_id         BIGINT(20)    NOT NULL                 COMMENT '评论用户ID',
    content         VARCHAR(1000) NOT NULL                 COMMENT '评论内容',
    floor_num       INT(11)       DEFAULT 1                COMMENT '楼层号',
    del_flag        CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 1删除）',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (comment_id),
    KEY idx_post_id (post_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='评论表';

-- ----------------------------
-- 用户禁言记录表
-- ----------------------------
DROP TABLE IF EXISTS forum_user_ban;
CREATE TABLE forum_user_ban (
    ban_id          BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    user_id         BIGINT(20)    NOT NULL                 COMMENT '被禁言用户ID',
    ban_days        INT(11)       NOT NULL                 COMMENT '禁言天数',
    ban_reason      VARCHAR(500)  DEFAULT ''               COMMENT '禁言原因',
    operator_id     BIGINT(20)    NOT NULL                 COMMENT '操作人ID',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (ban_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='用户禁言记录表';

-- ----------------------------
-- 论坛管理菜单
-- ----------------------------
INSERT INTO sys_menu VALUES(2000, '论坛管理', 0, 5, 'forum', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', SYSDATE(), '', NULL, '论坛管理目录');

-- 帖子管理菜单
INSERT INTO sys_menu VALUES(2001, '帖子管理', 2000, 1, 'post', 'forum/post/index', '', '', 1, 0, 'C', '0', '0', 'forum:post:list', 'documentation', 'admin', SYSDATE(), '', NULL, '帖子管理菜单');
INSERT INTO sys_menu VALUES(2002, '帖子查询', 2001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'forum:post:query', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES(2003, '帖子删除', 2001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'forum:post:remove', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES(2004, '帖子锁定', 2001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'forum:post:lock', '#', 'admin', SYSDATE(), '', NULL, '');

-- 用户管理菜单
INSERT INTO sys_menu VALUES(2010, '论坛用户', 2000, 2, 'user', 'forum/user/index', '', '', 1, 0, 'C', '0', '0', 'forum:user:list', 'user', 'admin', SYSDATE(), '', NULL, '论坛用户菜单');
INSERT INTO sys_menu VALUES(2011, '用户查询', 2010, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'forum:user:query', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES(2012, '用户禁言', 2010, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'forum:user:ban', '#', 'admin', SYSDATE(), '', NULL, '');
INSERT INTO sys_menu VALUES(2013, '解除禁言', 2010, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'forum:user:unban', '#', 'admin', SYSDATE(), '', NULL, '');
