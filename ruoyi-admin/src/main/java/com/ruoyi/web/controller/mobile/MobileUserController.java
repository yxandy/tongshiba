package com.ruoyi.web.controller.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.service.IForumUserService;
import com.ruoyi.web.dto.UserSyncRequest;

/**
 * 移动端用户接口
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/mobile/forum/user")
public class MobileUserController extends BaseController {

    @Autowired
    private IForumUserService forumUserService;

    /**
     * 同步企业微信用户
     */
    @PostMapping("/sync")
    public AjaxResult syncUser(@RequestBody UserSyncRequest request) {
        ForumUser user = forumUserService.syncWxUser(
                request.getWxUserid(),
                request.getNickname(),
                request.getAvatar());
        return success(user);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info/{wxUserid}")
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
}
