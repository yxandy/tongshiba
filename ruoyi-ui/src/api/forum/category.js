import request from '@/utils/request'

// 查询分类列表
export function listCategory(query) {
    return request({
        url: '/forum/category/list',
        method: 'get',
        params: query
    })
}

// 查询所有启用的分类
export function listAllCategory() {
    return request({
        url: '/forum/category/listAll',
        method: 'get'
    })
}

// 查询分类详细
export function getCategory(categoryId) {
    return request({
        url: '/forum/category/' + categoryId,
        method: 'get'
    })
}

// 新增分类
export function addCategory(data) {
    return request({
        url: '/forum/category',
        method: 'post',
        data: data
    })
}

// 修改分类
export function updateCategory(data) {
    return request({
        url: '/forum/category',
        method: 'put',
        data: data
    })
}

// 删除分类
export function delCategory(categoryId) {
    return request({
        url: '/forum/category/' + categoryId,
        method: 'delete'
    })
}
