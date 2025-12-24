package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.ForumComment;

/**
 * 评论Mapper接口
 * 
 * @author ruoyi
 */
public interface ForumCommentMapper {
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
     * 获取帖子当前最大楼层号
     */
    public Integer selectMaxFloorNumByPostId(Long postId);

    /**
     * 新增评论
     */
    public int insertForumComment(ForumComment forumComment);

    /**
     * 修改评论
     */
    public int updateForumComment(ForumComment forumComment);

    /**
     * 删除评论
     */
    public int deleteForumCommentById(Long commentId);

    /**
     * 批量删除评论
     */
    public int deleteForumCommentByIds(Long[] commentIds);

    /**
     * 根据帖子ID删除评论
     */
    public int deleteForumCommentByPostId(Long postId);

    /**
     * 逻辑删除评论并记录删除者
     */
    public int logicalDeleteById(@Param("commentId") Long commentId, @Param("deletedBy") Long deletedBy);
}
