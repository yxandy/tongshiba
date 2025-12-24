package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 评论对象 forum_comment
 * 
 * @author ruoyi
 */
public class ForumComment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 评论ID */
    private Long commentId;

    /** 帖子ID */
    private Long postId;

    /** 评论用户ID */
    private Long userId;

    /** 评论时用户所属单位 */
    private String userUnit;

    /** 评论时用户部门 */
    private String userDept;

    /** 评论内容 */
    @Excel(name = "评论内容")
    private String content;

    /** 楼层号 */
    @Excel(name = "楼层号")
    private Integer floorNum;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 删除者用户ID */
    private Long deletedBy;

    /** 评论用户信息（非数据库字段） */
    private ForumUser user;

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getFloorNum() {
        return floorNum;
    }

    public void setFloorNum(Integer floorNum) {
        this.floorNum = floorNum;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Long getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Long deletedBy) {
        this.deletedBy = deletedBy;
    }

    public ForumUser getUser() {
        return user;
    }

    public void setUser(ForumUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ForumComment{" +
                "commentId=" + commentId +
                ", postId=" + postId +
                ", userId=" + userId +
                ", floorNum=" + floorNum +
                '}';
    }
}
