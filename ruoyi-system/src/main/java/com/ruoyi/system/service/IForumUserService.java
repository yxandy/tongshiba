package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ForumUser;

/**
 * 论坛用户Service接口
 * 
 * @author ruoyi
 */
public interface IForumUserService {
    /**
     * 查询论坛用户
     */
    public ForumUser selectForumUserById(Long userId);

    /**
     * 根据企业微信ID查询用户
     */
    public ForumUser selectForumUserByWxUserid(String wxUserid);

    /**
     * 查询论坛用户列表
     */
    public List<ForumUser> selectForumUserList(ForumUser forumUser);

    /**
     * 新增论坛用户
     */
    public int insertForumUser(ForumUser forumUser);

    /**
     * 修改论坛用户
     */
    public int updateForumUser(ForumUser forumUser);

    /**
     * 同步企业微信用户（如存在则更新，不存在则新增）
     */
    public ForumUser syncWxUser(String wxUserid, String nickname, String avatar, String unit, String department);

    /**
     * 禁言用户
     */
    public int banUser(Long userId, Integer banDays, String reason, Long operatorId);

    /**
     * 解除禁言
     */
    public int unbanUser(Long userId);

    /**
     * 检查用户是否被禁言
     */
    public boolean isUserBanned(Long userId);

    /**
     * 删除论坛用户
     */
    public int deleteForumUserByIds(Long[] userIds);
}
