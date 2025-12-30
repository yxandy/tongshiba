package com.ruoyi.web.controller.mobile;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.ForumPost;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.domain.ForumPostEditHistory;
import com.ruoyi.system.service.IForumPostService;
import com.ruoyi.system.service.IForumUserService;
import com.ruoyi.system.service.IForumPostLogService;
import com.ruoyi.system.service.IVideoUrlService;
import com.ruoyi.system.service.IForumCategoryService;
import com.ruoyi.system.service.IForumPostEditHistoryService;
import com.ruoyi.system.domain.ForumCategory;
import com.ruoyi.web.dto.PostCreateRequest;
import com.ruoyi.web.dto.PostUpdateRequest;
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
    private ServerConfig serverConfig;

    @Autowired
    private IForumUserService forumUserService;

    @Autowired
    private IForumPostLogService forumPostLogService;

    @Autowired
    private IVideoUrlService videoUrlService;

    @Autowired
    private IForumCategoryService forumCategoryService;

    @Autowired
    private IForumPostEditHistoryService editHistoryService;

    /** 编辑时间窗口：10分钟（毫秒） */
    private static final long EDIT_TIME_WINDOW_MS = 10 * 60 * 1000;

    /**
     * 获取分类列表（启用状态的）
     */
    @GetMapping("/category/list")
    public AjaxResult listCategories() {
        List<ForumCategory> list = forumCategoryService.selectEnabledCategoryList();
        return success(list);
    }

    /**
     * 获取帖子列表
     */
    @GetMapping("/list")
    public TableDataInfo listPosts(ForumPost forumPost, @RequestParam(required = false) String wxUserid) {
        // 如果传入 wxUserid，则设置 currentUserId 用于限流过滤
        if (wxUserid != null && !wxUserid.isEmpty()) {
            ForumUser user = forumUserService.selectForumUserByWxUserid(wxUserid);
            if (user != null) {
                forumPost.setCurrentUserId(user.getUserId());
            }
        }
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
    public AjaxResult getPostDetail(@PathVariable Long postId, @RequestParam(required = false) String wxUserid) {
        ForumPost post = forumPostService.selectForumPostById(postId);
        if (post == null) {
            return error("帖子不存在");
        }

        // 限流检查：如果帖子被限流，仅作者可见
        if ("1".equals(post.getIsRestricted())) {
            if (wxUserid == null || wxUserid.isEmpty()) {
                return error("内容不存在");
            }
            ForumUser user = forumUserService.selectForumUserByWxUserid(wxUserid);
            if (user == null || !user.getUserId().equals(post.getUserId())) {
                return error("内容不存在");
            }
        }

        // 增加浏览次数
        forumPostService.incrementViewCount(postId);
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
        post.setCategoryId(request.getCategoryId());

        // 检查用户是否被限流，自动标记帖子
        boolean isAutoRestricted = false;
        if ("1".equals(user.getIsRateLimited())) {
            post.setIsRestricted("1");
            isAutoRestricted = true;
        }

        int result = forumPostService.insertForumPost(post);
        if (result > 0) {
            // 记录发帖日志
            forumPostLogService.logAction(post.getPostId(), "create", null, user.getNickname(), "用户发布帖子");

            // 如果是自动限流，记录限流日志
            if (isAutoRestricted) {
                forumPostLogService.logAction(post.getPostId(), "限流", null, "admin", "因用户当前处于限流状态，帖子自动限流");
            }
        }
        return toAjax(result);
    }

    /**
     * 更新帖子 (作者可在10分钟内编辑)
     */
    @PutMapping
    public AjaxResult updatePost(@RequestBody PostUpdateRequest request) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(request.getWxUserid());
        if (user == null) {
            return error("用户不存在");
        }

        ForumPost post = forumPostService.selectForumPostById(request.getPostId());
        if (post == null) {
            return error("帖子不存在");
        }

        // 权限检查: 只有作者可编辑
        if (!user.getUserId().equals(post.getUserId())) {
            return error("您没有权限编辑此帖子");
        }

        // 时间窗口检查: 10分钟内可编辑
        long elapsedTime = System.currentTimeMillis() - post.getCreateTime().getTime();
        if (elapsedTime > EDIT_TIME_WINDOW_MS) {
            return error("帖子发布超过10分钟，无法编辑");
        }

        // 保存编辑前的快照到历史表
        ForumPostEditHistory history = new ForumPostEditHistory();
        history.setPostId(post.getPostId());
        history.setUserId(user.getUserId());
        history.setTitle(post.getTitle());
        history.setContent(post.getContent());
        history.setImages(post.getImages());
        history.setVideoUrl(post.getVideoUrl());
        history.setCategoryId(post.getCategoryId());
        editHistoryService.insertEditHistory(history);

        // 更新帖子
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImages(request.getImages());
        // 解析视频短链接
        String videoUrl = request.getVideoUrl();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            videoUrl = videoUrlService.resolveShortUrl(videoUrl);
        }
        post.setVideoUrl(videoUrl);
        post.setCategoryId(request.getCategoryId());

        int result = forumPostService.updateForumPost(post);
        if (result > 0) {
            forumPostLogService.logAction(post.getPostId(), "edit", null, user.getNickname(), "用户编辑帖子");
        }
        return toAjax(result);
    }

    /**
     * 删除帖子 (作者、管理员、分级管理员可删除)
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

        // 权限检查
        boolean isAuthor = user.getUserId().equals(post.getUserId());
        boolean isAdmin = "admin".equals(user.getRole()) || "1".equals(user.getIsAdmin());
        boolean isSubAdmin = "sub_admin".equals(user.getRole());

        // 分级管理员只能删除本单位帖子
        boolean canSubAdminDelete = isSubAdmin && user.getUnit() != null && user.getUnit().equals(post.getUserUnit());

        if (!isAuthor && !isAdmin && !canSubAdminDelete) {
            return error("您没有权限删除此帖子");
        }

        int result = forumPostService.deleteForumPostById(request.getPostId());
        if (result > 0) {
            // 记录删帖日志，区分操作人角色
            String operatorRole = isAdmin ? "管理员" : (canSubAdminDelete ? "分级管理员" : "作者");
            forumPostLogService.logAction(request.getPostId(), "delete", null, user.getNickname(),
                    operatorRole + "删除帖子");
        }
        return toAjax(result);
    }

    /**
     * 上传图片（移动端专用）
     */
    @PostMapping("/upload/image")
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return error("上传文件不能为空");
            }
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return error("只能上传图片文件");
            }
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            // 返回可通过前端代理访问的 URL
            // 前端 /tongshiba-api 代理到后端 /tongshiba，后端静态资源路径是 /profile
            String accessUrl = "/tongshiba-api" + fileName;
            AjaxResult ajax = AjaxResult.success("上传成功");
            ajax.put("url", accessUrl);
            return ajax;
        } catch (Exception e) {
            return error("上传失败: " + e.getMessage());
        }
    }
}
