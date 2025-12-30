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

    @Autowired
    private com.ruoyi.system.service.IForumUnitService forumUnitService;

    @Autowired
    private com.ruoyi.system.service.IForumDepartmentService forumDepartmentService;

    @Override
    public ForumUser selectForumUserById(Long userId) {
        return forumUserMapper.selectForumUserById(userId);
    }

    @Override
    public ForumUser selectForumUserByWxUserid(String wxUserid) {
        return forumUserMapper.selectForumUserByWxUserid(wxUserid);
    }

    @Override
    public ForumUser selectForumUserByNickname(String nickname) {
        return forumUserMapper.selectForumUserByNickname(nickname);
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
    public ForumUser syncWxUser(String wxUserid, String nickname, String avatar, String unit, String department) {
        // 1. 同步单位表
        if (unit != null && !unit.isEmpty()) {
            forumUnitService.syncUnit(unit);
        }

        // 2. 同步部门表
        if (unit != null && !unit.isEmpty() && department != null && !department.isEmpty()) {
            // 先查询单位ID
            com.ruoyi.system.domain.ForumUnit forumUnit = forumUnitService.selectForumUnitByName(unit);
            if (forumUnit != null) {
                forumDepartmentService.syncDepartment(forumUnit.getUnitId(), department);
            }
        }

        // 3. 同步用户表
        ForumUser existingUser = forumUserMapper.selectForumUserByWxUserid(wxUserid);
        if (existingUser != null) {
            // 更新用户信息（包括单位和部门）
            existingUser.setNickname(nickname);
            existingUser.setAvatar(avatar);
            existingUser.setUnit(unit);
            existingUser.setDepartment(department);
            forumUserMapper.updateForumUser(existingUser);
            return existingUser;
        } else {
            // 新增用户
            ForumUser newUser = new ForumUser();
            newUser.setWxUserid(wxUserid);
            newUser.setNickname(nickname);
            newUser.setAvatar(avatar);
            newUser.setUnit(unit);
            newUser.setDepartment(department);
            newUser.setStatus("0");
            newUser.setRole("user"); // 默认角色为普通用户
            newUser.setIsRateLimited("0"); // 默认不限流
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
