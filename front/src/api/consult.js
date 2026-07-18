import request from '@/utils/request'

// 创建咨询
export function createConsult(data) {
    return request({
        url: '/consults',
        method: 'post',
        data
    })
}
// 我的咨询分页
export function getConsultList(params) {
    return request({
        url: '/consults',
        method: 'get',
        params
    })
}
// 详情
export function getConsultDetail(id) {
    return request({
        url: `/consults/${id}`,
        method: 'get'
    })
}
// 取消订单
export function cancelConsult(id) {
    return request({
        url: `/api/consults/${id}/cancel`,
        method: 'put'
    })
}
// 支付
export function payConsult(id, data) {
    return request({
        url: `/api/consults/${id}/pay`,
        method: 'post',
        data
    })
}