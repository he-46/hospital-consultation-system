import request from '@/utils/request'

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
export function getFollows(params) {
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