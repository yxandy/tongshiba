package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ForumComment;

/**
 * 评论Service接口
 * 
 * @author ruoyi
 */
public interface IForumCommentService {
    /**
     * 查询评论
     */
    public ForumComment selectForumCommentById(Long commentId);

    /**
     * 查询评论列表
     */
    public List<ForumComment> selectForumCommentList(ForumComment forumComment);

    /**
     * 根据帖子ID查询评论列表
     */
    public List<ForumComment> selectForumCommentByPostId(Long postId);

    /**
     * 新增评论
     */
    public int insertForumComment(ForumComment forumComment);

    /**
     * 删除评论
     */
    public int deleteForumCommentById(Long commentId);

    /**
     * 批量删除评论
     */
    public int deleteForumCommentByIds(Long[] commentIds);

    /**
     * 逻辑删除评论并记录删除者
     */
    public int deleteForumCommentByIdWithDeletedBy(Long commentId, Long deletedBy);
}
