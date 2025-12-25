package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ForumPost;

/**
 * 帖子Service接口
 * 
 * @author ruoyi
 */
public interface IForumPostService {
    /**
     * 查询帖子
     */
    public ForumPost selectForumPostById(Long postId);

    /**
     * 查询帖子列表
     */
    public List<ForumPost> selectForumPostList(ForumPost forumPost);

    /**
     * 新增帖子
     */
    public int insertForumPost(ForumPost forumPost);

    /**
     * 修改帖子
     */
    public int updateForumPost(ForumPost forumPost);

    /**
     * 删除帖子
     */
    public int deleteForumPostById(Long postId);

    /**
     * 批量删除帖子
     */
    public int deleteForumPostByIds(Long[] postIds);

    /**
     * 锁定帖子
     */
    public int lockPost(Long postId);

    /**
     * 解锁帖子
     */
    public int unlockPost(Long postId);

    /**
     * 增加浏览次数
     */
    public int incrementViewCount(Long postId);

    /**
     * 恢复已删除的帖子
     */
    public int restorePost(Long postId);

    /**
     * 置顶帖子
     * 
     * @param postId 帖子ID
     * @param hours  置顶时长（小时），0表示永久
     */
    public com.ruoyi.common.core.domain.AjaxResult pinPost(Long postId, Integer hours);

    /**
     * 取消置顶
     */
    public int unpinPost(Long postId);
}
