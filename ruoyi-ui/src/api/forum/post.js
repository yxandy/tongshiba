import request from '@/utils/request'

// 查询帖子列表
export function listPost(query) {
    return request({
        url: '/forum/post/list',
        method: 'get',
        params: query
    })
}

// 查询帖子详情
export function getPost(postId) {
    return request({
        url: '/forum/post/' + postId,
        method: 'get'
    })
}

// 删除帖子
export function delPost(postIds) {
    return request({
        url: '/forum/post/' + postIds,
        method: 'delete'
    })
}

// 锁定帖子
export function lockPost(postId) {
    return request({
        url: '/forum/post/lock/' + postId,
        method: 'put'
    })
}

// 解锁帖子
export function unlockPost(postId) {
    return request({
        url: '/forum/post/unlock/' + postId,
        method: 'put'
    })
}

// 恢复已删除帖子
export function restorePost(postId) {
    return request({
        url: '/forum/post/restore/' + postId,
        method: 'put'
    })
}

// 置顶帖子
export function pinPost(postId, hours) {
    return request({
        url: '/forum/post/pin/' + postId,
        method: 'put',
        params: { hours }
    })
}

// 取消置顶
export function unpinPost(postId) {
    return request({
        url: '/forum/post/unpin/' + postId,
        method: 'put'
    })
}

// 获取帖子操作日志
export function getPostLog(postId) {
    return request({
        url: '/forum/post/log/' + postId,
        method: 'get'
    })
}

// 获取帖子编辑历史
export function getEditHistory(postId) {
    return request({
        url: '/forum/post/edit-history/list/' + postId,
        method: 'get'
    })
}

// 获取帖子编辑次数
export function getEditHistoryCount(postId) {
    return request({
        url: '/forum/post/edit-history/count/' + postId,
        method: 'get'
    })
}
