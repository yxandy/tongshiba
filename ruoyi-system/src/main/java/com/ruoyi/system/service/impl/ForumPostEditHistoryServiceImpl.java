package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.ForumPostEditHistory;
import com.ruoyi.system.mapper.ForumPostEditHistoryMapper;
import com.ruoyi.system.service.IForumPostEditHistoryService;

/**
 * 帖子编辑历史Service实现
 */
@Service
public class ForumPostEditHistoryServiceImpl implements IForumPostEditHistoryService {

    @Autowired
    private ForumPostEditHistoryMapper editHistoryMapper;

    @Override
    public List<ForumPostEditHistory> selectEditHistoryByPostId(Long postId) {
        return editHistoryMapper.selectEditHistoryByPostId(postId);
    }

    @Override
    public ForumPostEditHistory selectEditHistoryById(Long historyId) {
        return editHistoryMapper.selectEditHistoryById(historyId);
    }

    @Override
    public int insertEditHistory(ForumPostEditHistory editHistory) {
        return editHistoryMapper.insertEditHistory(editHistory);
    }

    @Override
    public int countEditHistoryByPostId(Long postId) {
        return editHistoryMapper.countEditHistoryByPostId(postId);
    }
}
