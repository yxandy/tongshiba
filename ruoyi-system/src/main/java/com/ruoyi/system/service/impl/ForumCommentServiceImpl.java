package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.ForumComment;
import com.ruoyi.system.mapper.ForumCommentMapper;
import com.ruoyi.system.mapper.ForumPostMapper;
import com.ruoyi.system.service.IForumCommentService;

/**
 * 评论Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ForumCommentServiceImpl implements IForumCommentService {
    @Autowired
    private ForumCommentMapper forumCommentMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Override
    public ForumComment selectForumCommentById(Long commentId) {
        return forumCommentMapper.selectForumCommentById(commentId);
    }

    @Override
    public List<ForumComment> selectForumCommentList(ForumComment forumComment) {
        return forumCommentMapper.selectForumCommentList(forumComment);
    }

    @Override
    public List<ForumComment> selectForumCommentByPostId(Long postId) {
        return forumCommentMapper.selectForumCommentByPostId(postId);
    }

    @Override
    public List<ForumComment> selectForumCommentByPostIdWithFilter(Long postId, Long currentUserId) {
        List<ForumComment> allComments = forumCommentMapper.selectForumCommentByPostId(postId);

        // 如果没有传入当前用户ID，过滤掉所有限流评论
        if (currentUserId == null) {
            allComments.removeIf(comment -> "1".equals(comment.getIsRateLimited()));
            return allComments;
        }

        // 过滤限流评论：只保留非限流的，或者是当前用户自己发的
        allComments.removeIf(
                comment -> "1".equals(comment.getIsRateLimited()) && !currentUserId.equals(comment.getUserId()));

        return allComments;
    }

    @Override
    @Transactional
    public int insertForumComment(ForumComment forumComment) {
        // 获取当前最大楼层号
        Integer maxFloor = forumCommentMapper.selectMaxFloorNumByPostId(forumComment.getPostId());
        forumComment.setFloorNum(maxFloor + 1);

        // 插入评论
        int result = forumCommentMapper.insertForumComment(forumComment);

        // 增加帖子评论数
        forumPostMapper.incrementCommentCount(forumComment.getPostId());

        // 更新帖子最后回复时间
        forumPostMapper.updateLastReplyTime(forumComment.getPostId(), new java.util.Date());

        return result;
    }

    @Override
    @Transactional
    public int deleteForumCommentById(Long commentId) {
        ForumComment comment = forumCommentMapper.selectForumCommentById(commentId);
        if (comment != null) {
            // 减少帖子评论数
            forumPostMapper.decrementCommentCount(comment.getPostId());
        }
        return forumCommentMapper.deleteForumCommentById(commentId);
    }

    @Override
    @Transactional
    public int deleteForumCommentByIds(Long[] commentIds) {
        for (Long commentId : commentIds) {
            ForumComment comment = forumCommentMapper.selectForumCommentById(commentId);
            if (comment != null) {
                forumPostMapper.decrementCommentCount(comment.getPostId());
            }
        }
        return forumCommentMapper.deleteForumCommentByIds(commentIds);
    }

    @Override
    @Transactional
    public int deleteForumCommentByIdWithDeletedBy(Long commentId, Long deletedBy) {
        ForumComment comment = forumCommentMapper.selectForumCommentById(commentId);
        if (comment != null) {
            // 减少帖子评论数
            forumPostMapper.decrementCommentCount(comment.getPostId());
        }
        // 执行逻辑删除并记录删除者
        return forumCommentMapper.logicalDeleteById(commentId, deletedBy);
    }

    @Override
    @Transactional
    public int restoreComment(Long commentId) {
        ForumComment comment = forumCommentMapper.selectForumCommentById(commentId);
        if (comment != null && "1".equals(comment.getDelFlag())) {
            // 恢复评论
            int result = forumCommentMapper.restoreComment(commentId);
            // 恢复帖子评论数
            forumPostMapper.incrementCommentCount(comment.getPostId());
            return result;
        }
        return 0;
    }
}
