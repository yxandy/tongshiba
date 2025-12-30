-- =============================================
-- 论坛用户管理 - 操作按钮权限配置
-- =============================================
-- 说明：菜单"论坛管理 > 论坛用户"已存在，这里只添加操作按钮权限

-- 1. 查询"论坛用户"菜单ID（根据菜单名称和路径）
-- 注意：需要手动确认菜单ID，或者通过以下查询获取：
-- SELECT menu_id FROM sys_menu WHERE menu_name = '论坛用户' AND path = 'user' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '论坛管理');

-- 假设查询到的"论坛用户"菜单ID为 2001（请根据实际情况替换）
SET @userMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '论坛用户' AND path LIKE '%user%' LIMIT 1);

-- 2. 如果查询不到，说明菜单结构可能不同，请手动指定
-- SET @userMenuId = 你的实际菜单ID;

-- 3. 插入操作按钮权限（如果不存在）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '用户查询', @userMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:forumUser:query', '#', 'admin', SYSDATE(), '论坛用户查询权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:forumUser:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '用户修改', @userMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:forumUser:edit', '#', 'admin', SYSDATE(), '论坛用户修改权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:forumUser:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '分配角色', @userMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:forumUser:assignRole', '#', 'admin', SYSDATE(), '论坛用户分配角色权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:forumUser:assignRole');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '用户限流', @userMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:forumUser:rateLimit', '#', 'admin', SYSDATE(), '论坛用户限流权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:forumUser:rateLimit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '用户导出', @userMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'system:forumUser:export', '#', 'admin', SYSDATE(), '论坛用户导出权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'system:forumUser:export');

-- =============================================
-- 说明：如果按钮权限添加成功，刷新后台页面即可看到
-- 如果需要给特定角色分配权限，请在后台"角色管理"中手动勾选
-- =============================================
