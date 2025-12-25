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
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.ForumPost;
import com.ruoyi.system.service.IForumPostService;
import com.ruoyi.system.service.IForumPostLogService;

/**
 * 帖子管理Controller（管理后台）
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/forum/post")
public class ForumPostController extends BaseController {
    @Autowired
    private IForumPostService forumPostService;

    @Autowired
    private IForumPostLogService forumPostLogService;

    /**
     * 查询帖子列表
     */
    @PreAuthorize("@ss.hasPermi('forum:post:list')")
    @GetMapping("/list")
    public TableDataInfo list(ForumPost forumPost) {
        startPage();
        List<ForumPost> list = forumPostService.selectForumPostList(forumPost);
        return getDataTable(list);
    }

    /**
     * 获取帖子详情
     */
    @PreAuthorize("@ss.hasPermi('forum:post:query')")
    @GetMapping(value = "/{postId}")
    public AjaxResult getInfo(@PathVariable("postId") Long postId) {
        return success(forumPostService.selectForumPostById(postId));
    }

    /**
     * 查询帖子操作日志
     */
    @PreAuthorize("@ss.hasPermi('forum:post:list')")
    @GetMapping("/log/{postId}")
    public AjaxResult getLogList(@PathVariable Long postId) {
        return success(forumPostLogService.selectLogListByPostId(postId));
    }

    /**
     * 删除帖子
     */
    @PreAuthorize("@ss.hasPermi('forum:post:remove')")
    @Log(title = "帖子管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public AjaxResult remove(@PathVariable Long[] postIds) {
        int result = forumPostService.deleteForumPostByIds(postIds);
        if (result > 0) {
            String operatorName = SecurityUtils.getUsername();
            Long operatorId = SecurityUtils.getUserId();
            for (Long postId : postIds) {
                forumPostLogService.logAction(postId, "delete", operatorId, operatorName, "帖子被删除");
            }
        }
        return toAjax(result);
    }

    /**
     * 锁定帖子
     */
    @PreAuthorize("@ss.hasPermi('forum:post:lock')")
    @Log(title = "帖子管理", businessType = BusinessType.UPDATE)
    @PutMapping("/lock/{postId}")
    public AjaxResult lock(@PathVariable Long postId) {
        int result = forumPostService.lockPost(postId);
        if (result > 0) {
            forumPostLogService.logAction(postId, "lock", SecurityUtils.getUserId(), SecurityUtils.getUsername(),
                    "帖子被锁定");
        }
        return toAjax(result);
    }

    /**
     * 解锁帖子
     */
    @PreAuthorize("@ss.hasPermi('forum:post:lock')")
    @Log(title = "帖子管理", businessType = BusinessType.UPDATE)
    @PutMapping("/unlock/{postId}")
    public AjaxResult unlock(@PathVariable Long postId) {
        int result = forumPostService.unlockPost(postId);
        if (result > 0) {
            forumPostLogService.logAction(postId, "unlock", SecurityUtils.getUserId(), SecurityUtils.getUsername(),
                    "帖子被解锁");
        }
        return toAjax(result);
    }

    /**
     * 恢复已删除的帖子
     */
    @PreAuthorize("@ss.hasPermi('forum:post:remove')")
    @Log(title = "帖子管理", businessType = BusinessType.UPDATE)
    @PutMapping("/restore/{postId}")
    public AjaxResult restore(@PathVariable Long postId) {
        int result = forumPostService.restorePost(postId);
        if (result > 0) {
            forumPostLogService.logAction(postId, "restore", SecurityUtils.getUserId(), SecurityUtils.getUsername(),
                    "帖子被恢复");
        }
        return toAjax(result);
    }

    /**
     * 置顶帖子
     * 
     * @param postId 帖子ID
     * @param hours  置顶时长（小时），0表示永久
     */
    @PreAuthorize("@ss.hasPermi('forum:post:lock')")
    @Log(title = "帖子管理", businessType = BusinessType.UPDATE)
    @PutMapping("/pin/{postId}")
    public AjaxResult pin(@PathVariable Long postId, @RequestParam(defaultValue = "0") Integer hours) {
        AjaxResult result = forumPostService.pinPost(postId, hours);
        if (result.isSuccess()) {
            String desc = hours == 0 ? "帖子被永久置顶" : "帖子被置顶" + hours + "小时";
            forumPostLogService.logAction(postId, "pin", SecurityUtils.getUserId(), SecurityUtils.getUsername(), desc);
        }
        return result;
    }

    /**
     * 取消置顶
     */
    @PreAuthorize("@ss.hasPermi('forum:post:lock')")
    @Log(title = "帖子管理", businessType = BusinessType.UPDATE)
    @PutMapping("/unpin/{postId}")
    public AjaxResult unpin(@PathVariable Long postId) {
        int result = forumPostService.unpinPost(postId);
        if (result > 0) {
            forumPostLogService.logAction(postId, "unpin", SecurityUtils.getUserId(), SecurityUtils.getUsername(),
                    "帖子被取消置顶");
        }
        return toAjax(result);
    }
}
