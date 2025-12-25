package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ForumPostLog;

/**
 * 帖子操作日志Service接口
 * 
 * @author ruoyi
 */
public interface IForumPostLogService {
    /**
     * 根据帖子ID查询操作日志列表
     */
    public List<ForumPostLog> selectLogListByPostId(Long postId);

    /**
     * 记录操作日志
     */
    public int logAction(Long postId, String action, Long operatorId, String operatorName, String description);
}
