package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ForumCategory;

/**
 * 论坛分类Mapper接口
 */
public interface ForumCategoryMapper {

    /**
     * 查询分类
     */
    public ForumCategory selectForumCategoryById(Long categoryId);

    /**
     * 查询分类列表
     */
    public List<ForumCategory> selectForumCategoryList(ForumCategory forumCategory);

    /**
     * 查询启用的分类列表（按排序）
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
     * 删除分类
     */
    public int deleteForumCategoryById(Long categoryId);

    /**
     * 批量删除分类
     */
    public int deleteForumCategoryByIds(Long[] categoryIds);

    /**
     * 检查分类下是否有帖子
     */
    public int countPostsByCategoryId(Long categoryId);
}
