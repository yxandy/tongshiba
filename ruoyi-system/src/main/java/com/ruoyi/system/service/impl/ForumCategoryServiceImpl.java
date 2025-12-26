package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.ForumCategory;
import com.ruoyi.system.mapper.ForumCategoryMapper;
import com.ruoyi.system.service.IForumCategoryService;

/**
 * 论坛分类Service实现
 */
@Service
public class ForumCategoryServiceImpl implements IForumCategoryService {

    @Autowired
    private ForumCategoryMapper forumCategoryMapper;

    @Override
    public ForumCategory selectForumCategoryById(Long categoryId) {
        return forumCategoryMapper.selectForumCategoryById(categoryId);
    }

    @Override
    public List<ForumCategory> selectForumCategoryList(ForumCategory forumCategory) {
        return forumCategoryMapper.selectForumCategoryList(forumCategory);
    }

    @Override
    public List<ForumCategory> selectEnabledCategoryList() {
        return forumCategoryMapper.selectEnabledCategoryList();
    }

    @Override
    public int insertForumCategory(ForumCategory forumCategory) {
        return forumCategoryMapper.insertForumCategory(forumCategory);
    }

    @Override
    public int updateForumCategory(ForumCategory forumCategory) {
        return forumCategoryMapper.updateForumCategory(forumCategory);
    }

    @Override
    public int deleteForumCategoryByIds(Long[] categoryIds) {
        return forumCategoryMapper.deleteForumCategoryByIds(categoryIds);
    }

    @Override
    public boolean canDeleteCategory(Long categoryId) {
        int count = forumCategoryMapper.countPostsByCategoryId(categoryId);
        return count == 0;
    }
}
