import request from './index'

// 提交评价
export function submitReview(data) {
    return request({
        url: '/reviews',
        method: 'post',
        data
    })
}
// 我的评价
export function getReviewList(params) {
    return request({
        url: '/reviews',
        method: 'get',
        params
    })
}