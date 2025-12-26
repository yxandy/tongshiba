package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ForumCategory;

/**
 * 论坛分类Service接口
 */
public interface IForumCategoryService {

    /**
     * 查询分类
     */
    public ForumCategory selectForumCategoryById(Long categoryId);

    /**
     * 查询分类列表
     */
    public List<ForumCategory> selectForumCategoryList(ForumCategory forumCategory);

    /**
     * 查询启用的分类列表（供前端使用）
     */
    public List<ForumCategory> selectEnabledCategoryList();

    /**
     * 新增分类
     */
    public int insertForumCategory(ForumCategory forumCategory);

    /**
     * 修改分类
     */
    public int updateForumCategory(ForumCategory forumCategory);

    /**
     * 批量删除分类
     */
    public int deleteForumCategoryByIds(Long[] categoryIds);

    /**
     * 检查分类是否可以删除（无帖子关联时可删）
     */
    public boolean canDeleteCategory(Long categoryId);
}
