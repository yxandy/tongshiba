package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 帖子对象 forum_post
 * 
 * @author ruoyi
 */
public class ForumPost extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 帖子ID */
    private Long postId;

    /** 发帖用户ID */
    private Long userId;

    /** 发帖时用户所属单位 */
    private String userUnit;

    /** 发帖时用户部门 */
    private String userDept;

    /** 帖子标题 */
    @Excel(name = "帖子标题")
    private String title;

    /** 帖子内容 */
    private String content;

    /** 图片URL列表(JSON数组) */
    private String images;

    /** 视频链接 */
    private String videoUrl;

    /** 浏览次数 */
    @Excel(name = "浏览次数")
    private Integer viewCount;

    /** 评论数量 */
    @Excel(name = "评论数量")
    private Integer commentCount;

    /** 是否锁定（0否 1是） */
    @Excel(name = "是否锁定", readConverterExp = "0=否,1=是")
    private String isLocked;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 最后回复时间 */
    private java.util.Date lastReplyTime;

    /** 是否置顶（0否 1是） */
    private String isPinned;

    /** 置顶过期时间（NULL表示永久） */
    private java.util.Date pinExpireTime;

    /** 发帖用户信息（非数据库字段） */
    private ForumUser user;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public String getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(String isLocked) {
        this.isLocked = isLocked;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public ForumUser getUser() {
        return user;
    }

    public void setUser(ForumUser user) {
        this.user = user;
    }

    public java.util.Date getLastReplyTime() {
        return lastReplyTime;
    }

    public void setLastReplyTime(java.util.Date lastReplyTime) {
        this.lastReplyTime = lastReplyTime;
    }

    public String getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(String isPinned) {
        this.isPinned = isPinned;
    }

    public java.util.Date getPinExpireTime() {
        return pinExpireTime;
    }

    public void setPinExpireTime(java.util.Date pinExpireTime) {
        this.pinExpireTime = pinExpireTime;
    }

    @Override
    public String toString() {
        return "ForumPost{" +
                "postId=" + postId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", viewCount=" + viewCount +
                ", commentCount=" + commentCount +
                ", isLocked='" + isLocked + '\'' +
                '}';
    }
}
