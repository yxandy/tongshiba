package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.vo.ForumStatsVO;
import com.ruoyi.system.domain.vo.ForumStatsQuery;

/**
 * 帖子统计 Service接口
 * 
 * @author ruoyi
 */
public interface IForumStatisticsService {

    /**
     * 按单位统计发帖数据
     * 
     * @param query 查询参数
     * @return 单位统计列表
     */
    List<ForumStatsVO> selectUnitStats(ForumStatsQuery query);

    /**
     * 按部门统计发帖数据
     * 
     * @param query 查询参数
     * @return 部门统计列表
     */
    List<ForumStatsVO> selectDeptStats(ForumStatsQuery query);

    /**
     * 按用户统计发帖数据
     * 
     * @param query 查询参数
     * @return 用户统计列表
     */
    List<ForumStatsVO> selectUserStats(ForumStatsQuery query);
}
