package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 论坛用户对象 forum_user
 * 
 * @author ruoyi
 */
public class ForumUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 企业微信UserID */
    @Excel(name = "企业微信UserID")
    private String wxUserid;

    /** 用户昵称 */
    @Excel(name = "用户昵称")
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 部门 */
    @Excel(name = "部门")
    private String department;

    /** 状态（0正常 1禁言） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=禁言")
    private String status;

    /** 禁言结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date banEndTime;

    /** 是否管理员（0否 1是） */
    @Excel(name = "是否管理员", readConverterExp = "0=否,1=是")
    private String isAdmin;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getWxUserid() {
        return wxUserid;
    }

    public void setWxUserid(String wxUserid) {
        this.wxUserid = wxUserid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getBanEndTime() {
        return banEndTime;
    }

    public void setBanEndTime(Date banEndTime) {
        this.banEndTime = banEndTime;
    }

    public String getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(String isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        return "ForumUser{" +
                "userId=" + userId +
                ", wxUserid='" + wxUserid + '\'' +
                ", nickname='" + nickname + '\'' +
                ", department='" + department + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
