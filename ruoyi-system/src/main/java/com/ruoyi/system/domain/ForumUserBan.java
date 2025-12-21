package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户禁言记录对象 forum_user_ban
 * 
 * @author ruoyi
 */
public class ForumUserBan extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long banId;

    /** 被禁言用户ID */
    private Long userId;

    /** 禁言天数 */
    @Excel(name = "禁言天数")
    private Integer banDays;

    /** 禁言原因 */
    @Excel(name = "禁言原因")
    private String banReason;

    /** 操作人ID */
    private Long operatorId;

    /** 被禁言用户信息（非数据库字段） */
    private ForumUser user;

    public Long getBanId() {
        return banId;
    }

    public void setBanId(Long banId) {
        this.banId = banId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getBanDays() {
        return banDays;
    }

    public void setBanDays(Integer banDays) {
        this.banDays = banDays;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public ForumUser getUser() {
        return user;
    }

    public void setUser(ForumUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ForumUserBan{" +
                "banId=" + banId +
                ", userId=" + userId +
                ", banDays=" + banDays +
                ", banReason='" + banReason + '\'' +
                '}';
    }
}
