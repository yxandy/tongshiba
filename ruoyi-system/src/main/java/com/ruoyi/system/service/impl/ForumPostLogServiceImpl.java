package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.ForumPostLog;
import com.ruoyi.system.mapper.ForumPostLogMapper;
import com.ruoyi.system.service.IForumPostLogService;

/**
 * 帖子操作日志Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ForumPostLogServiceImpl implements IForumPostLogService {
    @Autowired
    private ForumPostLogMapper forumPostLogMapper;

    @Override
    public List<ForumPostLog> selectLogListByPostId(Long postId) {
        return forumPostLogMapper.selectLogListByPostId(postId);
    }

    @Override
    public int logAction(Long postId, String action, Long operatorId, String operatorName, String description) {
        ForumPostLog log = new ForumPostLog();
        log.setPostId(postId);
        log.setAction(action);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setDescription(description);
        return forumPostLogMapper.insertForumPostLog(log);
    }
}
