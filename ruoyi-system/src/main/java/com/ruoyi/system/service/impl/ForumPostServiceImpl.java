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
}
