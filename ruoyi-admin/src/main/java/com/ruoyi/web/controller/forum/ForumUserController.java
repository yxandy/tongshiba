package com.ruoyi.web.controller.forum;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.service.IForumUserService;

/**
 * 论坛用户Controller（管理后台）
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/forum/user")
public class ForumUserController extends BaseController {
    @Autowired
    private IForumUserService forumUserService;

    /**
     * 查询论坛用户列表
     */
    @PreAuthorize("@ss.hasPermi('forum:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(ForumUser forumUser) {
        startPage();
        List<ForumUser> list = forumUserService.selectForumUserList(forumUser);
        return getDataTable(list);
    }

    /**
     * 获取用户详情
     */
    @PreAuthorize("@ss.hasPermi('forum:user:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId) {
        return success(forumUserService.selectForumUserById(userId));
    }

    /**
     * 禁言用户
     */
    @PreAuthorize("@ss.hasPermi('forum:user:ban')")
    @Log(title = "用户禁言", businessType = BusinessType.UPDATE)
    @PostMapping("/ban")
    public AjaxResult ban(@RequestBody BanRequest request) {
        return toAjax(forumUserService.banUser(request.getUserId(), request.getBanDays(),
                request.getReason(), getUserId()));
    }

    /**
     * 解除禁言
     */
    @PreAuthorize("@ss.hasPermi('forum:user:unban')")
    @Log(title = "解除禁言", businessType = BusinessType.UPDATE)
    @PutMapping("/unban/{userId}")
    public AjaxResult unban(@PathVariable Long userId) {
        return toAjax(forumUserService.unbanUser(userId));
    }

    /**
     * 禁言请求参数
     */
    public static class BanRequest {
        private Long userId;
        private Integer banDays;
        private String reason;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Integer getBanDays() {
            return banDays;
        }

        public void setBanDays(Integer banDays) {
            this.banDays = banDays;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
