package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.ForumCommentLog;
import com.ruoyi.system.mapper.ForumCommentLogMapper;
import com.ruoyi.system.service.IForumCommentLogService;

/**
 * 评论操作日志Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ForumCommentLogServiceImpl implements IForumCommentLogService {
    @Autowired
    private ForumCommentLogMapper forumCommentLogMapper;

    @Override
    public List<ForumCommentLog> selectLogListByPostId(Long postId) {
        return forumCommentLogMapper.selectLogListByPostId(postId);
    }

    @Override
    public int logDelete(Long postId, Long commentId, Integer floorNum, String content, String operatorName) {
        ForumCommentLog log = new ForumCommentLog();
        log.setPostId(postId);
        log.setCommentId(commentId);
        log.setFloorNum(floorNum);
        // 截取内容摘要，最多50个字符
        if (content != null && content.length() > 50) {
            log.setContentSummary(content.substring(0, 50) + "...");
        } else {
            log.setContentSummary(content);
        }
        log.setOperatorName(operatorName);
        return forumCommentLogMapper.insertForumCommentLog(log);
    }
}
