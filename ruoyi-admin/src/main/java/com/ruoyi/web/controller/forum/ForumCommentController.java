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
import com.ruoyi.system.domain.ForumComment;
import com.ruoyi.system.service.IForumCommentService;

/**
 * 评论管理Controller（管理后台）
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/forum/comment")
public class ForumCommentController extends BaseController {

    @Autowired
    private IForumCommentService forumCommentService;

    /**
     * 查询评论列表
     */
    @PreAuthorize("@ss.hasPermi('forum:post:list')")
    @GetMapping("/list")
    public TableDataInfo list(ForumComment forumComment) {
        startPage();
        List<ForumComment> list = forumCommentService.selectForumCommentList(forumComment);
        return getDataTable(list);
    }

    /**
     * 根据帖子ID查询评论列表（包含已删除）
     */
    @PreAuthorize("@ss.hasPermi('forum:post:list')")
    @GetMapping("/listByPost/{postId}")
    public AjaxResult listByPost(@PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "0") String delFlag) {
        ForumComment query = new ForumComment();
        query.setPostId(postId);
        query.setDelFlag(delFlag);
        List<ForumComment> list = forumCommentService.selectForumCommentList(query);
        return success(list);
    }

    /**
     * 删除评论
     */
    @PreAuthorize("@ss.hasPermi('forum:post:remove')")
    @Log(title = "评论管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{commentIds}")
    public AjaxResult remove(@PathVariable Long[] commentIds) {
        return toAjax(forumCommentService.deleteForumCommentByIds(commentIds));
    }

    /**
     * 恢复已删除的评论
     */
    @PreAuthorize("@ss.hasPermi('forum:post:remove')")
    @Log(title = "评论管理", businessType = BusinessType.UPDATE)
    @PutMapping("/restore/{commentId}")
    public AjaxResult restore(@PathVariable Long commentId) {
        return toAjax(forumCommentService.restoreComment(commentId));
    }
}
