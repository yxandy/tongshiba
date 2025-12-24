package com.ruoyi.web.controller.mobile;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.ForumPost;
import com.ruoyi.system.domain.ForumPostFollow;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.mapper.ForumPostFollowMapper;
import com.ruoyi.system.mapper.ForumPostMapper;
import com.ruoyi.system.service.IForumUserService;
import com.ruoyi.web.dto.FollowRequest;

/**
 * 移动端帖子关注接口
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/mobile/forum/post")
public class MobileFollowController extends BaseController {

    @Autowired
    private IForumUserService forumUserService;

    @Autowired
    private ForumPostFollowMapper forumPostFollowMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    /**
     * 关注帖子
     */
    @PostMapping("/follow")
    public AjaxResult followPost(@RequestBody FollowRequest request) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(request.getWxUserid());
        if (user == null) {
            return error("用户不存在");
        }

        // 检查是否已关注
        ForumPostFollow existing = forumPostFollowMapper.selectByUserAndPost(user.getUserId(), request.getPostId());
        if (existing != null) {
            return success("已关注");
        }

        ForumPostFollow follow = new ForumPostFollow();
        follow.setUserId(user.getUserId());
        follow.setPostId(request.getPostId());
        forumPostFollowMapper.insertForumPostFollow(follow);
        return success("关注成功");
    }

    /**
     * 取消关注帖子
     */
    @PostMapping("/unfollow")
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
    @GetMapping("/follow/check/{postId}")
    public AjaxResult checkFollow(@PathVariable Long postId, @RequestParam String wxUserid) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(wxUserid);
        if (user == null) {
            return success(false);
        }

        ForumPostFollow follow = forumPostFollowMapper.selectByUserAndPost(user.getUserId(), postId);
        return success(follow != null);
    }

    /**
     * 获取关注的帖子列表
     */
    @GetMapping("/follow/list")
    public TableDataInfo listFollowedPosts(@RequestParam String wxUserid) {
        ForumUser user = forumUserService.selectForumUserByWxUserid(wxUserid);
        if (user == null) {
            return getDataTable(Collections.emptyList());
        }

        // 获取关注的帖子ID列表
        List<Long> postIds = forumPostFollowMapper.selectFollowedPostIdsByUserId(user.getUserId());
        if (postIds.isEmpty()) {
            return getDataTable(Collections.emptyList());
        }

        // 分页查询帖子详情 (已过滤已删除帖子)
        startPage();
        List<ForumPost> posts = forumPostMapper.selectFollowedPostList(postIds);
        return getDataTable(posts);
    }
}
