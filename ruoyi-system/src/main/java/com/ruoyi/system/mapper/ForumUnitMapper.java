package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.ForumUnit;

/**
 * 论坛单位Mapper接口
 * 
 * @author ruoyi
 */
public interface ForumUnitMapper {

    /**
     * 查询单位
     */
    public ForumUnit selectForumUnitById(Long unitId);

    /**
     * 根据单位名称查询
     */
    public ForumUnit selectForumUnitByName(String unitName);

    /**
     * 查询单位列表
     */
    public List<ForumUnit> selectForumUnitList(ForumUnit forumUnit);

    /**
     * 新增单位
     */
    public int insertForumUnit(ForumUnit forumUnit);

    /**
     * 修改单位
     */
    public int updateForumUnit(ForumUnit forumUnit);

    /**
     * 删除单位
     */
    public int deleteForumUnitById(Long unitId);
}
