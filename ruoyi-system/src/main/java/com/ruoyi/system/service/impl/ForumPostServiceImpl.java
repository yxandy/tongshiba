package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.ForumPost;
import com.ruoyi.system.mapper.ForumPostMapper;
import com.ruoyi.system.mapper.ForumCommentMapper;
import com.ruoyi.system.service.IForumPostService;

/**
 * 帖子Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ForumPostServiceImpl implements IForumPostService {
    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ForumCommentMapper forumCommentMapper;

    @Override
    public ForumPost selectForumPostById(Long postId) {
        return forumPostMapper.selectForumPostById(postId);
    }

    @Override
    public List<ForumPost> selectForumPostList(ForumPost forumPost) {
        return forumPostMapper.selectForumPostList(forumPost);
    }

    @Override
    public int insertForumPost(ForumPost forumPost) {
        return forumPostMapper.insertForumPost(forumPost);
    }

    @Override
    public int updateForumPost(ForumPost forumPost) {
        return forumPostMapper.updateForumPost(forumPost);
    }

    @Override
    @Transactional
    public int deleteForumPostById(Long postId) {
        // 同时删除帖子下的所有评论
        forumCommentMapper.deleteForumCommentByPostId(postId);
        return forumPostMapper.deleteForumPostById(postId);
    }

    @Override
    @Transactional
    public int deleteForumPostByIds(Long[] postIds) {
        for (Long postId : postIds) {
            forumCommentMapper.deleteForumCommentByPostId(postId);
        }
        return forumPostMapper.deleteForumPostByIds(postIds);
    }

    @Override
    public int lockPost(Long postId) {
        ForumPost post = new ForumPost();
        post.setPostId(postId);
        post.setIsLocked("1");
        return forumPostMapper.updateForumPost(post);
    }

    @Override
    public int unlockPost(Long postId) {
        ForumPost post = new ForumPost();
        post.setPostId(postId);
        post.setIsLocked("0");
        return forumPostMapper.updateForumPost(post);
    }

    @Override
    public int incrementViewCount(Long postId) {
        return forumPostMapper.incrementViewCount(postId);
    }

    @Override
    public int restorePost(Long postId) {
        ForumPost post = new ForumPost();
        post.setPostId(postId);
        post.setDelFlag("0");
        return forumPostMapper.updateForumPost(post);
    }

    @Override
    public com.ruoyi.common.core.domain.AjaxResult pinPost(Long postId, Integer hours) {
        // 检查帖子是否存在且未删除
        ForumPost existingPost = forumPostMapper.selectForumPostById(postId);
        if (existingPost == null) {
            return com.ruoyi.common.core.domain.AjaxResult.error("帖子不存在");
        }
        if ("1".equals(existingPost.getDelFlag())) {
            return com.ruoyi.common.core.domain.AjaxResult.error("已删除的帖子不能置顶");
        }

        ForumPost post = new ForumPost();
        post.setPostId(postId);
        post.setIsPinned("1");

        // 计算过期时间
        if (hours == null || hours == 0) {
            // 永久置顶
            post.setPinExpireTime(null);
        } else {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.HOUR_OF_DAY, hours);
            post.setPinExpireTime(calendar.getTime());
        }

        int result = forumPostMapper.updateForumPost(post);
        return result > 0 ? com.ruoyi.common.core.domain.AjaxResult.success()
                : com.ruoyi.common.core.domain.AjaxResult.error("置顶失败");
    }

    @Override
    public int unpinPost(Long postId) {
        ForumPost post = new ForumPost();
        post.setPostId(postId);
        post.setIsPinned("0");
        post.setPinExpireTime(null);
        return forumPostMapper.updateForumPost(post);
    }
}
