package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 帖子编辑历史对象 forum_post_edit_history
 */
public class ForumPostEditHistory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 历史ID */
    private Long historyId;

    /** 帖子ID */
    private Long postId;

    /** 编辑者ID */
    private Long userId;

    /** 编辑时的标题 */
    private String title;

    /** 编辑时的正文 */
    private String content;

    /** 编辑时的图片JSON */
    private String images;

    /** 编辑时的视频链接 */
    private String videoUrl;

    /** 编辑时的分类ID */
    private Long categoryId;

    /** 编辑时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date editTime;

    /** 分类名称（非数据库字段，用于展示） */
    private String categoryName;

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Date getEditTime() {
        return editTime;
    }

    public void setEditTime(Date editTime) {
        this.editTime = editTime;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
