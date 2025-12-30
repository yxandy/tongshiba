-- =============================================
-- 修正"论坛用户"菜单的路由配置
-- =============================================

-- 更新"论坛用户"菜单，指向正确的 Vue 组件
UPDATE sys_menu 
SET component = 'system/forumUser/index',
    perms = 'system:forumUser:list'
WHERE menu_name = '论坛用户' 
  AND path LIKE '%user%'
  AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '论坛管理') AS temp);

-- 查看更新结果
SELECT menu_id, menu_name, parent_id, path, component, perms 
FROM sys_menu 
WHERE menu_name = '论坛用户';
