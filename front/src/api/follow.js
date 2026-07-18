import request from './index'

// 新增关注
export function addFollow(data) {
    return request({
        url: '/follows',
        method: 'post',
        data
    })
}
// 取消关注
export function delFollow(id) {
    return request({
        url: `/follows/${id}`,
        method: 'delete'
    })
}
// 我的关注分页
export function getFollowList(params) {
    return request({
        url: '/follows',
        method: 'get',
        params
    })
}
// 判断是否关注
export function checkFollow(params) {
    return request({
        url: '/follows/check',
        method: 'get',
        params
    })
}

// 获取关注状态（别名）
export function getFollowStatus(followType, followId) {
    return checkFollow({ followType, followId })
}

// 获取关注记录ID（用于取消关注）
export function getFollowRecordId(followType, followId) {
    return request({
        url: '/follows/record-id',
        method: 'get',
        params: { followType, followId }
    })
}
