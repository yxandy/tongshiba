package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.ForumDepartment;

/**
 * 论坛部门Service接口
 * 
 * @author ruoyi
 */
public interface IForumDepartmentService {

    /**
     * 查询部门
     */
    public ForumDepartment selectForumDepartmentById(Long deptId);

    /**
     * 查询部门列表
     */
    public List<ForumDepartment> selectForumDepartmentList(ForumDepartment forumDepartment);

    /**
     * 根据单位ID查询部门列表
     */
    public List<ForumDepartment> selectDepartmentsByUnitId(Long unitId);

    /**
     * 新增部门
     */
    public int insertForumDepartment(ForumDepartment forumDepartment);

    /**
     * 修改部门
     */
    public int updateForumDepartment(ForumDepartment forumDepartment);

    /**
     * 删除部门
     */
    public int deleteForumDepartmentById(Long deptId);

    /**
     * 同步部门（如果不存在则创建，存在则返回）
     */
    public ForumDepartment syncDepartment(Long unitId, String deptName);
}
