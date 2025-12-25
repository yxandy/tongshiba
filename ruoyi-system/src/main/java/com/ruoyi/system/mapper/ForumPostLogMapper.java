package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ForumPostLog;

/**
 * 帖子操作日志Mapper接口
 * 
 * @author ruoyi
 */
public interface ForumPostLogMapper {
    /**
     * 根据帖子ID查询操作日志列表
     */
    public List<ForumPostLog> selectLogListByPostId(Long postId);

    /**
     * 新增操作日志
     */
    public int insertForumPostLog(ForumPostLog log);
}
