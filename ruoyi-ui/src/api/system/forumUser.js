import request from '@/utils/request'

// 查询论坛用户列表
export function listForumUser(query) {
    return request({
        url: '/system/forum/user/list',
        method: 'get',
        params: query
    })
}

// 查询论坛用户详细
export function getForumUser(userId) {
    return request({
        url: '/system/forum/user/' + userId,
        method: 'get'
    })
}

// 修改论坛用户
export function updateForumUser(data) {
    return request({
        url: '/system/forum/user',
        method: 'put',
        data: data
    })
}

// 分配角色
export function assignRole(data) {
    return request({
        url: '/system/forum/user/assignRole',
        method: 'put',
        data: data
    })
}

// 设置/解除限流
export function setRateLimit(data) {
    return request({
        url: '/system/forum/user/rateLimit',
        method: 'put',
        data: data
    })
}

// 获取单位列表
export function getUnitList() {
    return request({
        url: '/system/forum/user/unitList',
        method: 'get'
    })
}

// 获取部门列表
export function getDeptList(unitId) {
    return request({
        url: '/system/forum/user/deptList/' + unitId,
        method: 'get'
    })
}

// 导出论坛用户
export function exportForumUser(query) {
    return request({
        url: '/system/forum/user/export',
        method: 'post',
        params: query
    })
}
