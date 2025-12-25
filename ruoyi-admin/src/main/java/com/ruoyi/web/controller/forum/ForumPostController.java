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
import com.ruoyi.system.domain.ForumPost;
import com.ruoyi.system.service.IForumPostService;

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
     * 删除帖子
     */
    @PreAuthorize("@ss.hasPermi('forum:post:remove')")
    @Log(title = "帖子管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public AjaxResult remove(@PathVariable Long[] postIds) {
        return toAjax(forumPostService.deleteForumPostByIds(postIds));
    }

    /**
     * 锁定帖子
     */
    @PreAuthorize("@ss.hasPermi('forum:post:lock')")
    @Log(title = "帖子管理", businessType = BusinessType.UPDATE)
    @PutMapping("/lock/{postId}")
    public AjaxResult lock(@PathVariable Long postId) {
        return toAjax(forumPostService.lockPost(postId));
    }

    /**
     * 解锁帖子
     */
    @PreAuthorize("@ss.hasPermi('forum:post:lock')")
    @Log(title = "帖子管理", businessType = BusinessType.UPDATE)
    @PutMapping("/unlock/{postId}")
    public AjaxResult unlock(@PathVariable Long postId) {
        return toAjax(forumPostService.unlockPost(postId));
    }

    /**
     * 恢复已删除的帖子
     */
    @PreAuthorize("@ss.hasPermi('forum:post:remove')")
    @Log(title = "帖子管理", businessType = BusinessType.UPDATE)
    @PutMapping("/restore/{postId}")
    public AjaxResult restore(@PathVariable Long postId) {
        return toAjax(forumPostService.restorePost(postId));
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
        return forumPostService.pinPost(postId, hours);
    }

    /**
     * 取消置顶
     */
    @PreAuthorize("@ss.hasPermi('forum:post:lock')")
    @Log(title = "帖子管理", businessType = BusinessType.UPDATE)
    @PutMapping("/unpin/{postId}")
    public AjaxResult unpin(@PathVariable Long postId) {
        return toAjax(forumPostService.unpinPost(postId));
    }
}
