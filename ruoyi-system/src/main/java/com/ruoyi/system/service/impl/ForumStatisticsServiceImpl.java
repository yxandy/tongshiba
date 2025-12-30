package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.vo.ForumStatsVO;
import com.ruoyi.system.domain.vo.ForumStatsQuery;
import com.ruoyi.system.mapper.ForumStatisticsMapper;
import com.ruoyi.system.service.IForumStatisticsService;

/**
 * 帖子统计 Service实现
 * 
 * @author ruoyi
 */
@Service
public class ForumStatisticsServiceImpl implements IForumStatisticsService {

    @Autowired
    private ForumStatisticsMapper forumStatisticsMapper;

    @Override
    public List<ForumStatsVO> selectUnitStats(ForumStatsQuery query) {
        return forumStatisticsMapper.selectUnitStats(query);
    }

    @Override
    public List<ForumStatsVO> selectDeptStats(ForumStatsQuery query) {
        return forumStatisticsMapper.selectDeptStats(query);
    }

    @Override
    public List<ForumStatsVO> selectUserStats(ForumStatsQuery query) {
        return forumStatisticsMapper.selectUserStats(query);
    }
}
