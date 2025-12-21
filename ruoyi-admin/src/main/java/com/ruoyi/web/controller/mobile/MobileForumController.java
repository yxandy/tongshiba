package com.ruoyi.web.controller.mobile;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.ForumPost;
import com.ruoyi.system.domain.ForumComment;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.service.IForumPostService;
import com.ruoyi.system.service.IForumCommentService;
import com.ruoyi.system.service.IForumUserService;

/**
 * 移动端论坛接口
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/mobile/forum")
public class MobileForumController extends BaseController {
    @Autowired
    private IForumPostService forumPostService;

    @Autowired
    private IForumCommentService forumCommentService;

    @Autowired
    private IForumUserService forumUserService;

    /**
     * 同步企业微信用户
     */
    @PostMapping("/user/sync")
    public AjaxResult syncUser(@RequestBody UserSyncRequest request) {
        ForumUser user = forumUserService.syncWxUser(
                request.getWxUserid(),
                request.getNickname(),
                request.getAvatar(),
                request.getDepartment());
        return success(user);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/info/{wxUserid}")
    public AjaxResult getUserInfo(@PathVariable String wxUserid) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(wxUserid);
        if (user == null) {
            return error("用户不存在");
        }
        // 检查禁言状态
        if (forumUserService.isUserBanned(user.getUserId())) {
            user.setStatus("1");
        }
        return success(user);
    }

    /**
     * 获取帖子列表
     */
    @GetMapping("/post/list")
    public TableDataInfo listPosts(ForumPost forumPost) {
        startPage();
        List<ForumPost> list = forumPostService.selectForumPostList(forumPost);
        return getDataTable(list);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/post/{postId}")
    public AjaxResult getPostDetail(@PathVariable Long postId) {
        // 增加浏览次数
        forumPostService.incrementViewCount(postId);
        ForumPost post = forumPostService.selectForumPostById(postId);
        return success(post);
    }

    /**
     * 发布帖子
     */
    @PostMapping("/post")
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
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImages(request.getImages());

        return toAjax(forumPostService.insertForumPost(post));
    }

    /**
     * 获取帖子评论列表
     */
    @GetMapping("/comment/list/{postId}")
    public TableDataInfo listComments(@PathVariable Long postId) {
        startPage();
        List<ForumComment> list = forumCommentService.selectForumCommentByPostId(postId);
        return getDataTable(list);
    }

    /**
     * 发表评论
     */
    @PostMapping("/comment")
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

    // ==================== 关注帖子相关接口 ====================

    @Autowired
    private com.ruoyi.system.mapper.ForumPostFollowMapper forumPostFollowMapper;

    @Autowired
    private com.ruoyi.system.mapper.ForumPostMapper forumPostMapper;

    /**
     * 关注帖子
     */
    @PostMapping("/post/follow")
    public AjaxResult followPost(@RequestBody FollowRequest request) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(request.getWxUserid());
        if (user == null) {
            return error("用户不存在");
        }

        // 检查是否已关注
        com.ruoyi.system.domain.ForumPostFollow existing = forumPostFollowMapper.selectByUserAndPost(user.getUserId(),
                request.getPostId());
        if (existing != null) {
            return success("已关注");
        }

        com.ruoyi.system.domain.ForumPostFollow follow = new com.ruoyi.system.domain.ForumPostFollow();
        follow.setUserId(user.getUserId());
        follow.setPostId(request.getPostId());
        forumPostFollowMapper.insertForumPostFollow(follow);
        return success("关注成功");
    }

    /**
     * 取消关注帖子
     */
    @PostMapping("/post/unfollow")
    public AjaxResult unfollowPost(@RequestBody FollowRequest request) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(request.getWxUserid());
        if (user == null) {
            return error("用户不存在");
        }

        forumPostFollowMapper.deleteByUserAndPost(user.getUserId(), request.getPostId());
        return success("取消关注成功");
    }

    /**
     * 检查是否已关注帖子
     */
    @GetMapping("/post/follow/check/{postId}")
    public AjaxResult checkFollow(@PathVariable Long postId, @RequestParam String wxUserid) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(wxUserid);
        if (user == null) {
            return success(false);
        }

        com.ruoyi.system.domain.ForumPostFollow follow = forumPostFollowMapper.selectByUserAndPost(user.getUserId(),
                postId);
        return success(follow != null);
    }

    /**
     * 获取关注的帖子列表
     */
    @GetMapping("/post/follow/list")
    public TableDataInfo listFollowedPosts(@RequestParam String wxUserid) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(wxUserid);
        if (user == null) {
            return getDataTable(java.util.Collections.emptyList());
        }

        // 获取关注的帖子ID列表
        java.util.List<Long> postIds = forumPostFollowMapper.selectFollowedPostIdsByUserId(user.getUserId());
        if (postIds.isEmpty()) {
            return getDataTable(java.util.Collections.emptyList());
        }

        // 分页查询帖子详情 (已过滤已删除帖子)
        startPage();
        java.util.List<ForumPost> posts = forumPostMapper.selectFollowedPostList(postIds);
        return getDataTable(posts);
    }

    // ==================== 请求参数类 ====================

    public static class UserSyncRequest {
        private String wxUserid;
        private String nickname;
        private String avatar;
        private String department;

        public String getWxUserid() {
            return wxUserid;
        }

        public void setWxUserid(String wxUserid) {
            this.wxUserid = wxUserid;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }

    public static class PostCreateRequest {
        private String wxUserid;
        private String title;
        private String content;
        private String images;

        public String getWxUserid() {
            return wxUserid;
        }

        public void setWxUserid(String wxUserid) {
            this.wxUserid = wxUserid;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getImages() {
            return images;
        }

        public void setImages(String images) {
            this.images = images;
        }
    }

    public static class CommentCreateRequest {
        private String wxUserid;
        private Long postId;
        private String content;

        public String getWxUserid() {
            return wxUserid;
        }

        public void setWxUserid(String wxUserid) {
            this.wxUserid = wxUserid;
        }

        public Long getPostId() {
            return postId;
        }

        public void setPostId(Long postId) {
            this.postId = postId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class FollowRequest {
        private String wxUserid;
        private Long postId;

        public String getWxUserid() {
            return wxUserid;
        }

        public void setWxUserid(String wxUserid) {
            this.wxUserid = wxUserid;
        }

        public Long getPostId() {
            return postId;
        }

        public void setPostId(Long postId) {
            this.postId = postId;
        }
    }
}
