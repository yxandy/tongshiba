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
import com.ruoyi.system.domain.ForumCategory;
import com.ruoyi.system.service.IForumCategoryService;

/**
 * 论坛分类管理Controller
 */
@RestController
@RequestMapping("/forum/category")
public class ForumCategoryController extends BaseController {

    @Autowired
    private IForumCategoryService forumCategoryService;

    /**
     * 查询分类列表
     */
    @PreAuthorize("@ss.hasPermi('forum:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(ForumCategory forumCategory) {
        startPage();
        List<ForumCategory> list = forumCategoryService.selectForumCategoryList(forumCategory);
        return getDataTable(list);
    }

    /**
     * 查询所有启用的分类（下拉选择用）
     */
    @PreAuthorize("@ss.hasPermi('forum:category:list')")
    @GetMapping("/listAll")
    public AjaxResult listAll() {
        List<ForumCategory> list = forumCategoryService.selectEnabledCategoryList();
        return success(list);
    }

    /**
     * 获取分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('forum:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId) {
        return success(forumCategoryService.selectForumCategoryById(categoryId));
    }

    /**
     * 新增分类
     */
    @PreAuthorize("@ss.hasPermi('forum:category:add')")
    @Log(title = "论坛分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ForumCategory forumCategory) {
        return toAjax(forumCategoryService.insertForumCategory(forumCategory));
    }

    /**
     * 修改分类
     */
    @PreAuthorize("@ss.hasPermi('forum:category:edit')")
    @Log(title = "论坛分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ForumCategory forumCategory) {
        return toAjax(forumCategoryService.updateForumCategory(forumCategory));
    }

    /**
     * 删除分类
     */
    @PreAuthorize("@ss.hasPermi('forum:category:remove')")
    @Log(title = "论坛分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds) {
        // 检查是否有帖子关联
        for (Long categoryId : categoryIds) {
            if (!forumCategoryService.canDeleteCategory(categoryId)) {
                ForumCategory category = forumCategoryService.selectForumCategoryById(categoryId);
                return error("分类「" + category.getName() + "」下有帖子，无法删除");
            }
        }
        return toAjax(forumCategoryService.deleteForumCategoryByIds(categoryIds));
    }
}
