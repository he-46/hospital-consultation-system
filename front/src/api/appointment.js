import request from './index'

// 创建挂号订单
export function createAppointment(data) {
  return request({
    url: '/appointments',
    method: 'post',
    data
  })
}

// 获取我的挂号列表
export function getAppointmentList(params) {
  return request({
    url: '/appointments',
    method: 'get',
    params
  })
}

// 获取挂号详情
export function getAppointmentDetail(id) {
  return request({
    url: '/appointments/' + id,
    method: 'get'
  })
}

// 取消挂号
export function cancelAppointment(id) {
  return request({
    url: '/appointments/' + id + '/cancel',
    method: 'put'
  })
}

// 发起支付
export function payAppointment(id, data) {
  return request({
    url: '/appointments/' + id + '/pay',
    method: 'post',
    data
  })
}

// 支付回调
export function paymentCallback(orderNo) {
  return request({
    url: '/payment/callback',
    method: 'post',
    data: { orderNo }
  })
}
