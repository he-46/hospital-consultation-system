import request from './index'

// 获取医生列表
export function getDoctorList(params) {
  return request({
    url: '/doctor/list',
    method: 'get',
    params
  })
}

// 获取热门医生
export function getHotDoctors(limit = 10) {
  return request({
    url: '/doctor/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取医生详情
export function getDoctorDetail(id) {
  return request({
    url: '/doctor/detail',
    method: 'get',
    params: { id }
  })
}

// 搜索医生
export function searchDoctors(params) {
  return request({
    url: '/doctor/search',
    method: 'get',
    params
  })
}

// 获取医生排班
export function getDoctorSchedule(doctorId) {
  return request({
    url: '/doctor/schedule',
    method: 'get',
    params: { doctorId }
  })
}
