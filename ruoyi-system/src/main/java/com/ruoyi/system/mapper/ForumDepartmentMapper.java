package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ForumDepartment;
import org.apache.ibatis.annotations.Param;

/**
 * 论坛部门Mapper接口
 * 
 * @author ruoyi
 */
public interface ForumDepartmentMapper {

    /**
     * 查询部门
     */
    public ForumDepartment selectForumDepartmentById(Long deptId);

    /**
     * 根据单位ID和部门名称查询
     */
    public ForumDepartment selectByUnitAndName(@Param("unitId") Long unitId, @Param("deptName") String deptName);

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
}
