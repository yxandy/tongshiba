package com.ruoyi.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.ForumUser;
import com.ruoyi.system.domain.ForumUnit;
import com.ruoyi.system.domain.ForumDepartment;
import com.ruoyi.system.service.IForumUserService;
import com.ruoyi.system.service.IForumUnitService;
import com.ruoyi.system.service.IForumDepartmentService;

/**
 * 论坛用户管理Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/forum/user")
public class ForumUserController extends BaseController {

    @Autowired
    private IForumUserService forumUserService;

    @Autowired
    private IForumUnitService forumUnitService;

    @Autowired
    private IForumDepartmentService forumDepartmentService;

    /**
     * 获取论坛用户列表（支持分级管理员数据过滤）
     */
    @PreAuthorize("@ss.hasPermi('system:forumUser:list')")
    @GetMapping("/list")
    public TableDataInfo list(ForumUser forumUser) {
        // 获取当前登录用户的论坛角色信息
        String currentUsername = getUsername();
        ForumUser currentForumUser = forumUserService.selectForumUserByNickname(currentUsername);

        // 如果是分级管理员，限制只能查看本单位用户
        if (currentForumUser != null && "sub_admin".equals(currentForumUser.getRole())) {
            forumUser.setUnit(currentForumUser.getUnit());
        }

        startPage();
        List<ForumUser> list = forumUserService.selectForumUserList(forumUser);
        return getDataTable(list);
    }

    /**
     * 导出论坛用户列表
     */
    @Log(title = "论坛用户", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:forumUser:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, ForumUser forumUser) {
        List<ForumUser> list = forumUserService.selectForumUserList(forumUser);
        ExcelUtil<ForumUser> util = new ExcelUtil<ForumUser>(ForumUser.class);
        util.exportExcel(response, list, "论坛用户数据");
    }

    /**
     * 获取论坛用户详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:forumUser:query')")
    @GetMapping("/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId) {
        return success(forumUserService.selectForumUserById(userId));
    }

    /**
     * 修改论坛用户
     */
    @PreAuthorize("@ss.hasPermi('system:forumUser:edit')")
    @Log(title = "论坛用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ForumUser forumUser) {
        forumUser.setUpdateBy(getUsername());
        return toAjax(forumUserService.updateForumUser(forumUser));
    }

    /**
     * 分配角色
     */
    @PreAuthorize("@ss.hasPermi('system:forumUser:assignRole')")
    @Log(title = "分配角色", businessType = BusinessType.UPDATE)
    @PutMapping("/assignRole")
    public AjaxResult assignRole(@RequestBody ForumUser forumUser) {
        // 权限检查：只有 ruoyi-admin 可以分配 admin 角色
        String currentUsername = getUsername();
        if ("admin".equals(forumUser.getRole()) && !"admin".equals(currentUsername)) {
            return error("只有超级管理员可以分配管理员角色");
        }

        ForumUser updateUser = new ForumUser();
        updateUser.setUserId(forumUser.getUserId());
        updateUser.setRole(forumUser.getRole());
        updateUser.setUpdateBy(currentUsername);
        return toAjax(forumUserService.updateForumUser(updateUser));
    }

    /**
     * 设置/解除限流（分级管理员只能操作本单位用户）
     */
    @PreAuthorize("@ss.hasPermi('system:forumUser:rateLimit')")
    @Log(title = "用户限流", businessType = BusinessType.UPDATE)
    @PutMapping("/rateLimit")
    public AjaxResult rateLimit(@RequestBody ForumUser forumUser) {
        // 获取当前操作者的论坛角色
        String currentUsername = getUsername();
        ForumUser currentForumUser = forumUserService.selectForumUserByNickname(currentUsername);

        // 分级管理员只能操作本单位用户
        if (currentForumUser != null && "sub_admin".equals(currentForumUser.getRole())) {
            ForumUser targetUser = forumUserService.selectForumUserById(forumUser.getUserId());
            if (targetUser == null) {
                return error("目标用户不存在");
            }
            // 检查是否是同一单位
            String currentUnit = currentForumUser.getUnit();
            String targetUnit = targetUser.getUnit();
            if (currentUnit == null || !currentUnit.equals(targetUnit)) {
                return error("您只能对本单位用户进行限流操作");
            }
        }

        ForumUser updateUser = new ForumUser();
        updateUser.setUserId(forumUser.getUserId());
        updateUser.setIsRateLimited(forumUser.getIsRateLimited());
        updateUser.setUpdateBy(getUsername());
        return toAjax(forumUserService.updateForumUser(updateUser));
    }

    /**
     * 获取单位列表（下拉选择）
     */
    @GetMapping("/unitList")
    public AjaxResult unitList() {
        List<ForumUnit> list = forumUnitService.selectForumUnitList(new ForumUnit());
        return success(list);
    }

    /**
     * 根据单位ID获取部门列表（下拉选择）
     */
    @GetMapping("/deptList/{unitId}")
    public AjaxResult deptList(@PathVariable Long unitId) {
        List<ForumDepartment> list = forumDepartmentService.selectDepartmentsByUnitId(unitId);
        return success(list);
    }
}
