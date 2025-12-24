package com.ruoyi.web.dto;

/**
 * 发表评论请求DTO
 */
public class CommentCreateRequest {
    private String wxUserid;
    private Long postId;
    private String content;
    private String userUnit;
    private String userDept;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserUnit() {
        return userUnit;
    }

    public void setUserUnit(String userUnit) {
        this.userUnit = userUnit;
    }

    public String getUserDept() {
        return userDept;
    }

    public void setUserDept(String userDept) {
        this.userDept = userDept;
    }
}
