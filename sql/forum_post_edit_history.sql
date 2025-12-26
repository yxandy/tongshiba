-- 帖子编辑历史表
CREATE TABLE IF NOT EXISTS forum_post_edit_history (
  history_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '历史ID',
  post_id       BIGINT NOT NULL COMMENT '帖子ID',
  user_id       BIGINT NOT NULL COMMENT '编辑者ID',
  title         VARCHAR(200) COMMENT '编辑时的标题',
  content       TEXT COMMENT '编辑时的正文',
  images        LONGTEXT COMMENT '编辑时的图片JSON',
  video_url     VARCHAR(500) COMMENT '编辑时的视频链接',
  category_id   BIGINT COMMENT '编辑时的分类ID',
  edit_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子编辑历史表';
