package com.ruoyi.web.controller.mobile;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.ForumPost;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.service.IForumPostService;
import com.ruoyi.system.service.IForumUserService;
import com.ruoyi.system.service.IForumPostLogService;
import com.ruoyi.system.service.IVideoUrlService;
import com.ruoyi.web.dto.PostCreateRequest;
import com.ruoyi.web.dto.DeletePostRequest;

/**
 * 移动端帖子接口
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/mobile/forum/post")
public class MobilePostController extends BaseController {

    @Autowired
    private IForumPostService forumPostService;

    @Autowired
    private IForumUserService forumUserService;

    @Autowired
    private IForumPostLogService forumPostLogService;

    @Autowired
    private IVideoUrlService videoUrlService;

    /**
     * 获取帖子列表
     */
    @GetMapping("/list")
    public TableDataInfo listPosts(ForumPost forumPost) {
        startPage();
        List<ForumPost> list = forumPostService.selectForumPostList(forumPost);
        // 列表页不需要图片数据，用标识替代以减少传输量
        for (ForumPost post : list) {
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                post.setImages("1"); // 标识有图片
            }
        }
        return getDataTable(list);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{postId}")
    public AjaxResult getPostDetail(@PathVariable Long postId) {
        // 增加浏览次数
        forumPostService.incrementViewCount(postId);
        ForumPost post = forumPostService.selectForumPostById(postId);
        return success(post);
    }

    /**
     * 发布帖子
     */
    @PostMapping("")
    public AjaxResult createPost(@RequestBody PostCreateRequest request) {
        // 检查用户是否被禁言
        ForumUser user = forumUserService.selectForumUserByWxUserid(request.getWxUserid());
        if (user == null) {
            return error("用户不存在，请先同步用户信息");
        }
        if (forumUserService.isUserBanned(user.getUserId())) {
            return error("您已被禁言，无法发帖");
        }

        ForumPost post = new ForumPost();
        post.setUserId(user.getUserId());
        post.setUserUnit(request.getUserUnit());
        post.setUserDept(request.getUserDept());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImages(request.getImages());
        // 解析视频短链接
        String videoUrl = request.getVideoUrl();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            videoUrl = videoUrlService.resolveShortUrl(videoUrl);
        }
        post.setVideoUrl(videoUrl);

        int result = forumPostService.insertForumPost(post);
        if (result > 0) {
            // 记录发帖日志
            forumPostLogService.logAction(post.getPostId(), "create", null, user.getNickname(), "用户发布帖子");
        }
        return toAjax(result);
    }

    /**
     * 删除帖子 (作者或管理员可删除)
     */
    @PostMapping("/delete")
    public AjaxResult deletePost(@RequestBody DeletePostRequest request) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(request.getWxUserid());
        if (user == null) {
            return error("用户不存在");
        }

        ForumPost post = forumPostService.selectForumPostById(request.getPostId());
        if (post == null) {
            return error("帖子不存在");
        }

        // 权限检查: 作者或管理员可删除
        boolean isAuthor = user.getUserId().equals(post.getUserId());
        boolean isAdmin = "1".equals(user.getIsAdmin());

        if (!isAuthor && !isAdmin) {
            return error("您没有权限删除此帖子");
        }

        int result = forumPostService.deleteForumPostById(request.getPostId());
        if (result > 0) {
            // 记录删帖日志
            forumPostLogService.logAction(request.getPostId(), "delete", null, user.getNickname(), "帖子被删除");
        }
        return toAjax(result);
    }
}
