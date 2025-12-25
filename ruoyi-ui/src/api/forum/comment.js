import request from '@/utils/request'

// 根据帖子ID查询评论列表
export function listCommentByPost(postId, delFlag = '0') {
    return request({
        url: '/forum/comment/listByPost/' + postId,
        method: 'get',
        params: { delFlag }
    })
}

// 删除评论
export function delComment(commentIds) {
    return request({
        url: '/forum/comment/' + commentIds,
        method: 'delete'
    })
}

// 恢复已删除评论
export function restoreComment(commentId) {
    return request({
        url: '/forum/comment/restore/' + commentId,
        method: 'put'
    })
}

// 获取评论删除日志
export function getCommentLog(postId) {
    return request({
        url: '/forum/comment/log/' + postId,
        method: 'get'
    })
}
