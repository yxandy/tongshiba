package com.ruoyi.web.controller.forum;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.domain.vo.ForumStatsVO;
import com.ruoyi.system.domain.vo.ForumStatsQuery;
import com.ruoyi.system.service.IForumStatisticsService;
import com.ruoyi.system.service.IForumUserService;

/**
 * 帖子数据统计Controller（管理后台）
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/forum/statistics")
public class ForumStatisticsController extends BaseController {

    @Autowired
    private IForumStatisticsService forumStatisticsService;

    @Autowired
    private IForumUserService forumUserService;

    /**
     * 获取当前登录用户的论坛用户信息（用于分级管理员权限判断）
     */
    private ForumUser getCurrentForumUser() {
        String currentUsername = getUsername();
        return forumUserService.selectForumUserByNickname(currentUsername);
    }

    /**
     * 应用分级管理员数据权限
     * 如果当前用户是分级管理员，强制设置unitName为用户所属单位
     */
    private void applyDataScope(ForumStatsQuery query) {
        ForumUser currentUser = getCurrentForumUser();
        if (currentUser != null && "sub_admin".equals(currentUser.getRole())) {
            query.setUnitName(currentUser.getUnit());
        }
    }

    /**
     * 按单位统计（第一层）
     */
    @PreAuthorize("@ss.hasPermi('forum:statistics:list')")
    @GetMapping("/unit")
    public AjaxResult listUnitStats(ForumStatsQuery query) {
        applyDataScope(query);
        List<ForumStatsVO> list = forumStatisticsService.selectUnitStats(query);
        return success(list);
    }

    /**
     * 按部门统计（第二层）
     */
    @PreAuthorize("@ss.hasPermi('forum:statistics:list')")
    @GetMapping("/dept")
    public AjaxResult listDeptStats(ForumStatsQuery query) {
        applyDataScope(query);
        // 分级管理员必须有unitName，普通查询也需要指定unitName
        if (query.getUnitName() == null || query.getUnitName().isEmpty()) {
            return error("请指定单位");
        }
        List<ForumStatsVO> list = forumStatisticsService.selectDeptStats(query);
        return success(list);
    }

    /**
     * 按用户统计（第二层切换）
     */
    @PreAuthorize("@ss.hasPermi('forum:statistics:list')")
    @GetMapping("/user")
    public AjaxResult listUserStats(ForumStatsQuery query) {
        applyDataScope(query);
        if (query.getUnitName() == null || query.getUnitName().isEmpty()) {
            return error("请指定单位");
        }
        List<ForumStatsVO> list = forumStatisticsService.selectUserStats(query);
        return success(list);
    }

    /**
     * 导出单位统计数据
     */
    @Log(title = "数据统计", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('forum:statistics:export')")
    @PostMapping("/export/unit")
    public void exportUnitStats(HttpServletResponse response, ForumStatsQuery query) {
        applyDataScope(query);
        List<ForumStatsVO> list = forumStatisticsService.selectUnitStats(query);
        ExcelUtil<ForumStatsVO> util = new ExcelUtil<ForumStatsVO>(ForumStatsVO.class);
        util.exportExcel(response, list, "单位发帖统计");
    }

    /**
     * 导出部门统计数据
     */
    @Log(title = "数据统计", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('forum:statistics:export')")
    @PostMapping("/export/dept")
    public void exportDeptStats(HttpServletResponse response, ForumStatsQuery query) {
        applyDataScope(query);
        List<ForumStatsVO> list = forumStatisticsService.selectDeptStats(query);
        ExcelUtil<ForumStatsVO> util = new ExcelUtil<ForumStatsVO>(ForumStatsVO.class);
        util.exportExcel(response, list, "部门发帖统计");
    }

    /**
     * 导出用户统计数据
     */
    @Log(title = "数据统计", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('forum:statistics:export')")
    @PostMapping("/export/user")
    public void exportUserStats(HttpServletResponse response, ForumStatsQuery query) {
        applyDataScope(query);
        List<ForumStatsVO> list = forumStatisticsService.selectUserStats(query);
        ExcelUtil<ForumStatsVO> util = new ExcelUtil<ForumStatsVO>(ForumStatsVO.class);
        util.exportExcel(response, list, "人员发帖统计");
    }
}
