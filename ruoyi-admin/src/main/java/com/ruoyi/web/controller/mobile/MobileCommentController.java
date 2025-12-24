package com.ruoyi.web.controller.mobile;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.ForumComment;
import com.ruoyi.system.domain.ForumPost;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.service.IForumCommentService;
import com.ruoyi.system.service.IForumPostService;
import com.ruoyi.system.service.IForumUserService;
import com.ruoyi.web.dto.CommentCreateRequest;
import com.ruoyi.web.dto.CommentDeleteRequest;

/**
 * 移动端评论接口
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/mobile/forum/comment")
public class MobileCommentController extends BaseController {

    @Autowired
    private IForumCommentService forumCommentService;

    @Autowired
    private IForumPostService forumPostService;

    @Autowired
    private IForumUserService forumUserService;

    /**
     * 获取帖子评论列表
     */
    @GetMapping("/list/{postId}")
    public TableDataInfo listComments(@PathVariable Long postId) {
        startPage();
        List<ForumComment> list = forumCommentService.selectForumCommentByPostId(postId);
        return getDataTable(list);
    }

    /**
     * 发表评论
     */
    @PostMapping("")
    public AjaxResult createComment(@RequestBody CommentCreateRequest request) {
        // 检查帖子是否存在且未锁定
        ForumPost post = forumPostService.selectForumPostById(request.getPostId());
        if (post == null) {
            return error("帖子不存在");
        }
        if ("1".equals(post.getIsLocked())) {
            return error("帖子已锁定，无法评论");
        }

        // 检查用户是否被禁言
        ForumUser user = forumUserService.selectForumUserByWxUserid(request.getWxUserid());
        if (user == null) {
            return error("用户不存在，请先同步用户信息");
        }
        if (forumUserService.isUserBanned(user.getUserId())) {
            return error("您已被禁言，无法评论");
        }

        ForumComment comment = new ForumComment();
        comment.setPostId(request.getPostId());
        comment.setUserId(user.getUserId());
        comment.setContent(request.getContent());

        return toAjax(forumCommentService.insertForumComment(comment));
    }

    /**
     * 删除评论 (评论作者或管理员可删除)
     */
    @PostMapping("/delete")
    public AjaxResult deleteComment(@RequestBody CommentDeleteRequest request) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(request.getWxUserid());
        if (user == null) {
            return error("用户不存在");
        }

        ForumComment comment = forumCommentService.selectForumCommentById(request.getCommentId());
        if (comment == null) {
            return error("评论不存在");
        }

        // 权限检查: 评论作者或管理员可删除
        boolean isAuthor = user.getUserId().equals(comment.getUserId());
        boolean isAdmin = "1".equals(user.getIsAdmin());

        if (!isAuthor && !isAdmin) {
            return error("您没有权限删除此评论");
        }

        // 执行逻辑删除，记录删除者
        return toAjax(
                forumCommentService.deleteForumCommentByIdWithDeletedBy(request.getCommentId(), user.getUserId()));
    }
}
