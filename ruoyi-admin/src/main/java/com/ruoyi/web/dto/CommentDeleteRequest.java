package com.ruoyi.web.dto;

/**
 * 删除评论请求DTO
 */
public class CommentDeleteRequest {
    private String wxUserid;
    private Long commentId;

    public String getWxUserid() {
        return wxUserid;
    }

    public void setWxUserid(String wxUserid) {
        this.wxUserid = wxUserid;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }
}
