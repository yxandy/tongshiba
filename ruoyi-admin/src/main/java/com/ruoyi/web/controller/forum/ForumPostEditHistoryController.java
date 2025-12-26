package com.ruoyi.web.controller.forum;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.ForumPostEditHistory;
import com.ruoyi.system.service.IForumPostEditHistoryService;

/**
 * 帖子编辑历史Controller
 */
@RestController
@RequestMapping("/forum/post/edit-history")
public class ForumPostEditHistoryController extends BaseController {

    @Autowired
    private IForumPostEditHistoryService editHistoryService;

    /**
     * 查询帖子编辑历史列表
     */
    @PreAuthorize("@ss.hasPermi('forum:post:list')")
    @GetMapping("/list/{postId}")
    public AjaxResult list(@PathVariable Long postId) {
        List<ForumPostEditHistory> list = editHistoryService.selectEditHistoryByPostId(postId);
        return success(list);
    }

    /**
     * 查询编辑历史详情
     */
    @PreAuthorize("@ss.hasPermi('forum:post:list')")
    @GetMapping("/{historyId}")
    public AjaxResult getInfo(@PathVariable Long historyId) {
        ForumPostEditHistory history = editHistoryService.selectEditHistoryById(historyId);
        return success(history);
    }

    /**
     * 统计帖子编辑次数
     */
    @PreAuthorize("@ss.hasPermi('forum:post:list')")
    @GetMapping("/count/{postId}")
    public AjaxResult count(@PathVariable Long postId) {
        int count = editHistoryService.countEditHistoryByPostId(postId);
        return success(count);
    }
}
