package com.ruoyi.web.dto;

/**
 * 关注帖子请求DTO
 */
public class FollowRequest {
    private String wxUserid;
    private Long postId;

    public String getWxUserid() {
        return wxUserid;
    }

    public void setWxUserid(String wxUserid) {
        this.wxUserid = wxUserid;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
