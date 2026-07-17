import request from '@/utils/request'

// 提交评价
export function submitReview(data) {
    return request({
        url: '/api/reviews',
        method: 'post',
        data
    })
}
// 我的评价
export function getReviewList(params) {
    return request({
        url: '/api/reviews',
        method: 'get',
        params
    })
}