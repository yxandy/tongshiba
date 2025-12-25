package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ForumCommentLog;

/**
 * 评论操作日志Service接口
 * 
 * @author ruoyi
 */
public interface IForumCommentLogService {
    /**
     * 根据帖子ID查询评论操作日志列表
     */
    public List<ForumCommentLog> selectLogListByPostId(Long postId);

    /**
     * 记录评论删除日志
     */
    public int logDelete(Long postId, Long commentId, Integer floorNum, String content, String operatorName);
}
