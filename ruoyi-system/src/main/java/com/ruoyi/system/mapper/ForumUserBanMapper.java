package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ForumUserBan;

/**
 * 用户禁言记录Mapper接口
 * 
 * @author ruoyi
 */
public interface ForumUserBanMapper {
    /**
     * 查询禁言记录
     */
    public ForumUserBan selectForumUserBanById(Long banId);

    /**
     * 查询禁言记录列表
     */
    public List<ForumUserBan> selectForumUserBanList(ForumUserBan forumUserBan);

    /**
     * 根据用户ID查询禁言记录
     */
    public List<ForumUserBan> selectForumUserBanByUserId(Long userId);

    /**
     * 新增禁言记录
     */
    public int insertForumUserBan(ForumUserBan forumUserBan);

    /**
     * 删除禁言记录
     */
    public int deleteForumUserBanById(Long banId);
}
