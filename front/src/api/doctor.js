import request from './index'

// 获取医生分页列表
export function getDoctorList(params) {
  return request({
    url: '/doctors',
    method: 'get',
    params
  })
}

// 获取热门医生
export function getHotDoctors(limit = 10) {
  return request({
    url: '/doctors/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取医生详情
export function getDoctorDetail(id) {
  return request({
    url: `/doctors/${id}`,
    method: 'get'
  })
}

// 搜索医生
export function searchDoctors(params) {
  return request({
    url: '/doctors/search',
    method: 'get',
    params
  })
}

// 获取医生排班
export function getDoctorSchedule(doctorId) {
  return request({
    url: `/doctors/${doctorId}/schedules`,
    method: 'get'
  })
}

// 获取医生评价列表
export function getDoctorReviews(doctorId, params = {}) {
  return request({
    url: `/doctors/${doctorId}/reviews`,
    method: 'get',
    params
  })
}

// 某科室下的医生列表
export function getDoctorsByDepartment(departmentId, params = {}) {
  return request({
    url: `/department/${departmentId}/doctors`,
    method: 'get',
    params
  })
}

// 某医院下的医生列表
export function getDoctorsByHospital(hospitalId, params = {}) {
  return request({
    url: `/hospital/${hospitalId}/doctors`,
    method: 'get',
    params
  })
}

// 查询是否关注医生 followType=2代表医生
export const getFollowStatus = (doctorId) => {
  return request({
    url: `/follows/check`,
    method: 'get',
    params: {
      followType: 2,
      followId: doctorId
    }
  })
}

// 关注医生
export const followDoctor = (doctorId) => {
  return request({
    url: `/follows`,
    method: 'post',
    data: {
      followType: 2,
      followId: doctorId
    }
  })
}

// 取消关注（传入follow记录的主键id）
export const unfollowDoctor = (followRecordId) => {
  return request({
    url: `/follows/${followRecordId}`,
    method: 'delete'
  })
}