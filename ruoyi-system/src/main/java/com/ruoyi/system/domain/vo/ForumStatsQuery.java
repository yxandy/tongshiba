package com.ruoyi.system.domain.vo;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 统计查询参数
 * 
 * @author ruoyi
 */
public class ForumStatsQuery {

    /** 开始日期 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /** 截止日期 */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /** 单位名称（用于第二层查询） */
    private String unitName;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
}
