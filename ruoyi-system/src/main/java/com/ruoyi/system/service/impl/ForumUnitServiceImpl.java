package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.ForumUnit;
import com.ruoyi.system.mapper.ForumUnitMapper;
import com.ruoyi.system.service.IForumUnitService;

/**
 * 论坛单位Service实现
 * 
 * @author ruoyi
 */
@Service
public class ForumUnitServiceImpl implements IForumUnitService {

    @Autowired
    private ForumUnitMapper forumUnitMapper;

    @Override
    public ForumUnit selectForumUnitById(Long unitId) {
        return forumUnitMapper.selectForumUnitById(unitId);
    }

    @Override
    public ForumUnit selectForumUnitByName(String unitName) {
        return forumUnitMapper.selectForumUnitByName(unitName);
    }

    @Override
    public List<ForumUnit> selectForumUnitList(ForumUnit forumUnit) {
        return forumUnitMapper.selectForumUnitList(forumUnit);
    }

    @Override
    public int insertForumUnit(ForumUnit forumUnit) {
        return forumUnitMapper.insertForumUnit(forumUnit);
    }

    @Override
    public int updateForumUnit(ForumUnit forumUnit) {
        return forumUnitMapper.updateForumUnit(forumUnit);
    }

    @Override
    public int deleteForumUnitById(Long unitId) {
        return forumUnitMapper.deleteForumUnitById(unitId);
    }

    @Override
    public ForumUnit syncUnit(String unitName) {
        // 查询是否已存在
        ForumUnit existing = forumUnitMapper.selectForumUnitByName(unitName);
        if (existing != null) {
            return existing;
        }

        // 不存在则创建
        ForumUnit unit = new ForumUnit();
        unit.setUnitName(unitName);

        // 特殊规则：山东高速总部显示为"总部"
        if ("山东高速股份有限公司".equals(unitName)) {
            unit.setDisplayName("总部");
        } else {
            unit.setDisplayName(unitName);
        }

        forumUnitMapper.insertForumUnit(unit);
        return unit;
    }
}
