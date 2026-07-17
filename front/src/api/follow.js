import request from '@/utils/request'

// 新增关注
export function addFollow(data) {
    return request({
        url: '/api/follows',
        method: 'post',
        data
    })
}
// 取消关注
export function delFollow(id) {
    return request({
        url: `/api/follows/${id}`,
        method: 'delete'
    })
}
// 我的关注分页
export function getFollowList(params) {
    return request({
        url: '/api/follows',
        method: 'get',
        params
    })
}
// 判断是否关注
export function checkFollow(params) {
    return request({
        url: '/api/follows/check',
        method: 'get',
        params
    })
}