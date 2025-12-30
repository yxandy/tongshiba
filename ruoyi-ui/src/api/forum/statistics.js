import request from '@/utils/request'

// 按单位统计
export function listUnitStats(query) {
    return request({
        url: '/forum/statistics/unit',
        method: 'get',
        params: query
    })
}

// 按部门统计
export function listDeptStats(query) {
    return request({
        url: '/forum/statistics/dept',
        method: 'get',
        params: query
    })
}

// 按用户统计
export function listUserStats(query) {
    return request({
        url: '/forum/statistics/user',
        method: 'get',
        params: query
    })
}
