package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 论坛部门对象 forum_department
 * 
 * @author ruoyi
 */
public class ForumDepartment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 部门ID */
    private Long deptId;

    /** 所属单位ID */
    @Excel(name = "所属单位ID")
    private Long unitId;

    /** 部门名称 */
    @Excel(name = "部门名称")
    private String deptName;

    /** 所属单位名称（关联查询） */
    private String unitName;

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public String toString() {
        return "ForumDepartment{" +
                "deptId=" + deptId +
                ", unitId=" + unitId +
                ", deptName='" + deptName + '\'' +
                '}';
    }
}
