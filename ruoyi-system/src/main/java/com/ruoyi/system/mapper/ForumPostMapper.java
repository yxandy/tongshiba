package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ForumPost;

/**
 * 帖子Mapper接口
 * 
 * @author ruoyi
 */
public interface ForumPostMapper {
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
     * 增加浏览次数
     */
    public int incrementViewCount(Long postId);

    /**
     * 增加评论数量
     */
    public int incrementCommentCount(Long postId);

    /**
     * 减少评论数量
     */
    public int decrementCommentCount(Long postId);

    /**
     * 查询用户关注的帖子列表
     */
    public List<ForumPost> selectFollowedPostList(@org.apache.ibatis.annotations.Param("postIds") List<Long> postIds);

    /**
     * 更新帖子最后回复时间
     */
    public int updateLastReplyTime(@org.apache.ibatis.annotations.Param("postId") Long postId,
            @org.apache.ibatis.annotations.Param("lastReplyTime") java.util.Date lastReplyTime);
}
