package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ForumUser;

/**
 * 论坛用户Mapper接口
 * 
 * @author ruoyi
 */
public interface ForumUserMapper {
    /**
     * 查询论坛用户
     */
    public ForumUser selectForumUserById(Long userId);

    /**
     * 根据企业微信ID查询用户
     */
    public ForumUser selectForumUserByWxUserid(String wxUserid);

    /**
     * 根据昵称查询用户
     */
    public ForumUser selectForumUserByNickname(String nickname);

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
     * 删除论坛用户
     */
    public int deleteForumUserById(Long userId);

    /**
     * 批量删除论坛用户
     */
    public int deleteForumUserByIds(Long[] userIds);
}
