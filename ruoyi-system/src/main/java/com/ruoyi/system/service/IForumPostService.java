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
}
