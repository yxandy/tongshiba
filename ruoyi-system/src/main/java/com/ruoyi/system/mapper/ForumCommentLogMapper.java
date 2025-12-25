package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ForumCommentLog;

/**
 * 评论操作日志Mapper接口
 * 
 * @author ruoyi
 */
public interface ForumCommentLogMapper {
    /**
     * 根据帖子ID查询评论操作日志列表
     */
    public List<ForumCommentLog> selectLogListByPostId(Long postId);

    /**
     * 新增操作日志
     */
    public int insertForumCommentLog(ForumCommentLog log);
}
