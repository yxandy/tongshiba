package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ForumPostEditHistory;

/**
 * 帖子编辑历史Service接口
 */
public interface IForumPostEditHistoryService {

    /**
     * 查询帖子编辑历史列表
     */
    List<ForumPostEditHistory> selectEditHistoryByPostId(Long postId);

    /**
     * 查询帖子编辑历史详情
     */
    ForumPostEditHistory selectEditHistoryById(Long historyId);

    /**
     * 新增帖子编辑历史
     */
    int insertEditHistory(ForumPostEditHistory editHistory);

    /**
     * 统计帖子编辑次数
     */
    int countEditHistoryByPostId(Long postId);
}
