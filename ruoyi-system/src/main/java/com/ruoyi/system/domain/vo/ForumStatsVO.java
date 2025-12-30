package com.ruoyi.system.domain.vo;

import com.ruoyi.common.annotation.Excel;

/**
 * 帖子统计数据 VO
 * 
 * @author ruoyi
 */
public class ForumStatsVO {

    /** 单位名称 */
    @Excel(name = "单位名称")
    private String unitName;

    /** 部门名称 */
    @Excel(name = "部门名称")
    private String deptName;

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    @Excel(name = "发帖人")
    private String userName;

    /** 发帖数量 */
    @Excel(name = "发帖数量")
    private Integer postCount;

    /** 置顶数量 */
    @Excel(name = "置顶数量")
    private Integer pinCount;

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Integer getPinCount() {
        return pinCount;
    }

    public void setPinCount(Integer pinCount) {
        this.pinCount = pinCount;
    }
}
