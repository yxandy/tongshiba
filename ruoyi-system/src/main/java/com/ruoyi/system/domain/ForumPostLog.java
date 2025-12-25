package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 帖子操作日志对象 forum_post_log
 * 
 * @author ruoyi
 */
public class ForumPostLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 帖子ID */
    private Long postId;

    /** 操作类型：create/delete/restore/pin/unpin/lock/unlock */
    private String action;

    /** 操作人ID（管理员用户ID） */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 操作描述 */
    private String description;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }
}
