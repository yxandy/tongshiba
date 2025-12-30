package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.ForumDepartment;
import com.ruoyi.system.mapper.ForumDepartmentMapper;
import com.ruoyi.system.service.IForumDepartmentService;

/**
 * 论坛部门Service实现
 * 
 * @author ruoyi
 */
@Service
public class ForumDepartmentServiceImpl implements IForumDepartmentService {

    @Autowired
    private ForumDepartmentMapper forumDepartmentMapper;

    @Override
    public ForumDepartment selectForumDepartmentById(Long deptId) {
        return forumDepartmentMapper.selectForumDepartmentById(deptId);
    }

    @Override
    public List<ForumDepartment> selectForumDepartmentList(ForumDepartment forumDepartment) {
        return forumDepartmentMapper.selectForumDepartmentList(forumDepartment);
    }

    @Override
    public List<ForumDepartment> selectDepartmentsByUnitId(Long unitId) {
        return forumDepartmentMapper.selectDepartmentsByUnitId(unitId);
    }

    @Override
    public int insertForumDepartment(ForumDepartment forumDepartment) {
        return forumDepartmentMapper.insertForumDepartment(forumDepartment);
    }

    @Override
    public int updateForumDepartment(ForumDepartment forumDepartment) {
        return forumDepartmentMapper.updateForumDepartment(forumDepartment);
    }

    @Override
    public int deleteForumDepartmentById(Long deptId) {
        return forumDepartmentMapper.deleteForumDepartmentById(deptId);
    }

    @Override
    public ForumDepartment syncDepartment(Long unitId, String deptName) {
        // 查询是否已存在
        ForumDepartment existing = forumDepartmentMapper.selectByUnitAndName(unitId, deptName);
        if (existing != null) {
            return existing;
        }

        // 不存在则创建
        ForumDepartment department = new ForumDepartment();
        department.setUnitId(unitId);
        department.setDeptName(deptName);
        forumDepartmentMapper.insertForumDepartment(department);
        return department;
    }
}
