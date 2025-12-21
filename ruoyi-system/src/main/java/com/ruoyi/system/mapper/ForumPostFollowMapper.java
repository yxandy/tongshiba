package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ForumPostFollow;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子关注Mapper接口
 */
public interface ForumPostFollowMapper {

    /**
     * 新增关注
     */
    public int insertForumPostFollow(ForumPostFollow forumPostFollow);

    /**
     * 删除关注
     */
    public int deleteByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 查询用户是否关注了某帖子
     */
    public ForumPostFollow selectByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 查询用户关注的帖子ID列表
     */
    public List<Long> selectFollowedPostIdsByUserId(Long userId);
}
