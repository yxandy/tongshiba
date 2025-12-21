package com.ruoyi.system.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.domain.ForumUserBan;
import com.ruoyi.system.mapper.ForumUserMapper;
import com.ruoyi.system.mapper.ForumUserBanMapper;
import com.ruoyi.system.service.IForumUserService;

/**
 * 论坛用户Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class ForumUserServiceImpl implements IForumUserService {
    @Autowired
    private ForumUserMapper forumUserMapper;

    @Autowired
    private ForumUserBanMapper forumUserBanMapper;

    @Override
    public ForumUser selectForumUserById(Long userId) {
        return forumUserMapper.selectForumUserById(userId);
    }

    @Override
    public ForumUser selectForumUserByWxUserid(String wxUserid) {
        return forumUserMapper.selectForumUserByWxUserid(wxUserid);
    }

    @Override
    public List<ForumUser> selectForumUserList(ForumUser forumUser) {
        return forumUserMapper.selectForumUserList(forumUser);
    }

    @Override
    public int insertForumUser(ForumUser forumUser) {
        return forumUserMapper.insertForumUser(forumUser);
    }

    @Override
    public int updateForumUser(ForumUser forumUser) {
        return forumUserMapper.updateForumUser(forumUser);
    }

    @Override
    @Transactional
    public ForumUser syncWxUser(String wxUserid, String nickname, String avatar, String department) {
        ForumUser existingUser = forumUserMapper.selectForumUserByWxUserid(wxUserid);
        if (existingUser != null) {
            // 更新用户信息
            existingUser.setNickname(nickname);
            existingUser.setAvatar(avatar);
            existingUser.setDepartment(department);
            forumUserMapper.updateForumUser(existingUser);
            return existingUser;
        } else {
            // 新增用户
            ForumUser newUser = new ForumUser();
            newUser.setWxUserid(wxUserid);
            newUser.setNickname(nickname);
            newUser.setAvatar(avatar);
            newUser.setDepartment(department);
            newUser.setStatus("0");
            forumUserMapper.insertForumUser(newUser);
            return newUser;
        }
    }

    @Override
    @Transactional
    public int banUser(Long userId, Integer banDays, String reason, Long operatorId) {
        // 计算禁言结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, banDays);
        Date banEndTime = calendar.getTime();

        // 更新用户状态
        ForumUser user = new ForumUser();
        user.setUserId(userId);
        user.setStatus("1");
        user.setBanEndTime(banEndTime);
        forumUserMapper.updateForumUser(user);

        // 记录禁言历史
        ForumUserBan ban = new ForumUserBan();
        ban.setUserId(userId);
        ban.setBanDays(banDays);
        ban.setBanReason(reason);
        ban.setOperatorId(operatorId);
        return forumUserBanMapper.insertForumUserBan(ban);
    }

    @Override
    public int unbanUser(Long userId) {
        ForumUser user = new ForumUser();
        user.setUserId(userId);
        user.setStatus("0");
        user.setBanEndTime(null);
        return forumUserMapper.updateForumUser(user);
    }

    @Override
    public boolean isUserBanned(Long userId) {
        ForumUser user = forumUserMapper.selectForumUserById(userId);
        if (user == null) {
            return false;
        }
        if (!"1".equals(user.getStatus())) {
            return false;
        }
        // 检查禁言是否过期
        if (user.getBanEndTime() != null && user.getBanEndTime().before(new Date())) {
            // 禁言已过期，自动解禁
            unbanUser(userId);
            return false;
        }
        return true;
    }

    @Override
    public int deleteForumUserByIds(Long[] userIds) {
        return forumUserMapper.deleteForumUserByIds(userIds);
    }
}
