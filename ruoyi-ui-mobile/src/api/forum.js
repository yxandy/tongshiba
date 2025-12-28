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

// 获取帖子详情（silentError: 限流帖子返回错误时不弹提示）
export function getPostDetail(postId, wxUserid) {
    return request({
        url: `/mobile/forum/post/${postId}`,
        method: 'get',
        params: wxUserid ? { wxUserid } : {},
        silentError: true
    })
}

// 获取分类列表
export function getCategoryList() {
    return request({
        url: '/mobile/forum/post/category/list',
        method: 'get'
    })
}

// 发布帖子（图片已提前上传，无需长超时）
export function createPost(data) {
    return request({
        url: '/mobile/forum/post',
        method: 'post',
        data
    })
}

// 更新帖子
export function updatePost(data) {
    return request({
        url: '/mobile/forum/post',
        method: 'put',
        data
    })
}

// 上传图片（移动端专用，无需认证）
export function uploadImage(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/mobile/forum/post/upload/image',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: onProgress,
        timeout: 60000
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

// 获取用户发过的帖子列表
export function getMyPosts(userId, params) {
    return request({
        url: '/mobile/forum/post/list',
        method: 'get',
        params: { userId, ...params }
    })
}

// 删除帖子
export function deletePost(data) {
    return request({
        url: '/mobile/forum/post/delete',
        method: 'post',
        data
    })
}

// 删除评论
export function deleteComment(data) {
    return request({
        url: '/mobile/forum/comment/delete',
        method: 'post',
        data  // { commentId, wxUserid (删除者) }
    })
}
