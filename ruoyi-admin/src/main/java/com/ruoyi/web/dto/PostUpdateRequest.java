package com.ruoyi.web.dto;

/**
 * 更新帖子请求DTO
 */
public class PostUpdateRequest {
    /** 帖子ID */
    private Long postId;
    /** 企业微信userid */
    private String wxUserid;
    /** 标题 */
    private String title;
    /** 正文内容 */
    private String content;
    /** 图片（逗号分隔） */
    private String images;
    /** 视频链接 */
    private String videoUrl;
    /** 分类ID */
    private Long categoryId;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getWxUserid() {
        return wxUserid;
    }

    public void setWxUserid(String wxUserid) {
        this.wxUserid = wxUserid;
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
}
