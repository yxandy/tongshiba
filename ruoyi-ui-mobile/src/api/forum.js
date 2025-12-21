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

// 关注帖子
export function followPost(data) {
    return request({
        url: '/mobile/forum/post/follow',
        method: 'post',
        data
    })
}

// 取消关注帖子
export function unfollowPost(data) {
    return request({
        url: '/mobile/forum/post/unfollow',
        method: 'post',
        data
    })
}

// 检查是否已关注帖子
export function checkFollow(postId, wxUserid) {
    return request({
        url: `/mobile/forum/post/follow/check/${postId}`,
        method: 'get',
        params: { wxUserid }
    })
}

// 获取关注的帖子列表
export function getFollowedPosts(wxUserid, params) {
    return request({
        url: '/mobile/forum/post/follow/list',
        method: 'get',
        params: { wxUserid, ...params }
    })
}
