package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 帖子关注对象 forum_post_follow
 */
public class ForumPostFollow extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 关注ID */
    private Long followId;

    /** 用户ID */
    private Long userId;

    /** 帖子ID */
    private Long postId;

    public Long getFollowId() {
        return followId;
    }

    public void setFollowId(Long followId) {
        this.followId = followId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
