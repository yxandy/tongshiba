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
import com.ruoyi.system.domain.ForumComment;
import com.ruoyi.system.service.IForumCommentService;
import com.ruoyi.system.service.IForumCommentLogService;

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

    @Autowired
    private IForumCommentLogService forumCommentLogService;

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
     * 查询评论删除日志
     */
    @PreAuthorize("@ss.hasPermi('forum:post:list')")
    @GetMapping("/log/{postId}")
    public AjaxResult getLogList(@PathVariable Long postId) {
        return success(forumCommentLogService.selectLogListByPostId(postId));
    }

    /**
     * 删除评论
     */
    @PreAuthorize("@ss.hasPermi('forum:post:remove')")
    @Log(title = "评论管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{commentIds}")
    public AjaxResult remove(@PathVariable Long[] commentIds) {
        String operatorName = SecurityUtils.getUsername();
        // 先查询评论信息用于记录日志
        for (Long commentId : commentIds) {
            ForumComment comment = forumCommentService.selectForumCommentById(commentId);
            if (comment != null) {
                forumCommentLogService.logDelete(
                        comment.getPostId(),
                        commentId,
                        comment.getFloorNum(),
                        comment.getContent(),
                        operatorName);
            }
        }
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
