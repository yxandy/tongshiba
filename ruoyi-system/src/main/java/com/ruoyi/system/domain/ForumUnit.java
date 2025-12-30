package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 论坛单位对象 forum_unit
 * 
 * @author ruoyi
 */
public class ForumUnit extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 单位ID */
    private Long unitId;

    /** 单位名称（原始名称） */
    @Excel(name = "单位名称")
    private String unitName;

    /** 显示名称（总部等特殊名称） */
    @Excel(name = "显示名称")
    private String displayName;

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "ForumUnit{" +
                "unitId=" + unitId +
                ", unitName='" + unitName + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
