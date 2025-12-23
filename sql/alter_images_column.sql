-- 修改 forum_post 表的 images 列为 LONGTEXT，以支持存储 Base64 编码的图片数据
ALTER TABLE forum_post MODIFY COLUMN images LONGTEXT COMMENT '图片URL列表(JSON数组，支持Base64)';
