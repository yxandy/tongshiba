package com.ruoyi.web.dto;

/**
 * 删除帖子请求DTO
 */
public class DeletePostRequest {
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
