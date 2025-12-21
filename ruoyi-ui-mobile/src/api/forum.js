import request from '@/utils/request'

// 同步用户信息
export function syncUser(data) {
    return request({
        url: '/mobile/forum/user/sync',
        method: 'post',
        data
    })
}

// 获取用户信息
export function getUserInfo(wxUserid) {
    return request({
        url: `/mobile/forum/user/info/${wxUserid}`,
        method: 'get'
    })
}

// 获取帖子列表
export function getPostList(params) {
    return request({
        url: '/mobile/forum/post/list',
        method: 'get',
        params
    })
}

// 获取帖子详情
export function getPostDetail(postId) {
    return request({
        url: `/mobile/forum/post/${postId}`,
        method: 'get'
    })
}

// 发布帖子
export function createPost(data) {
    return request({
        url: '/mobile/forum/post',
        method: 'post',
        data
    })
}

// 获取评论列表
export function getCommentList(postId, params) {
    return request({
        url: `/mobile/forum/comment/list/${postId}`,
        method: 'get',
        params
    })
}

// 发表评论
export function createComment(data) {
    return request({
        url: '/mobile/forum/comment',
        method: 'post',
        data
    })
}
