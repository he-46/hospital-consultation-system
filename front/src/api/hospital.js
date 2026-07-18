import request from './index'

// 获取医院列表
export function getHospitalList(params) {
  return request({
    url: '/hospital/list',
    method: 'get',
    params
  })
}

// 2-3 医院分页列表（新接口，支持等级筛选）
export function getHospitals(params) {
  return request({
    url: '/hospital/hospitals',
    method: 'get',
    params
  })
}

// 获取热门医院
export function getHotHospitals(limit = 8) {
  return request({
    url: '/hospital/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取医院详情（原接口）
export function getHospitalDetail(id) {
  return request({
    url: '/hospital/detail',
    method: 'get',
    params: { id }
  })
}

// 2-5 医院详情（RESTful 新接口）
export function getHospitalById(id) {
  return request({
    url: `/hospital/hospitals/${id}`,
    method: 'get'
  })
}

// 搜索医院
export function searchHospitals(params) {
  return request({
    url: '/hospital/search',
    method: 'get',
    params
  })
}

// 2-4 搜索医院（新接口）
export function searchHospitalsNew(params) {
  return request({
    url: '/hospital/hospitals/search',
    method: 'get',
    params
  })
}

// 2-6 某医院下的科室列表
export function getHospitalDepartments(hospitalId) {
  return request({
    url: `/hospital/hospitals/${hospitalId}/departments`,
    method: 'get'
  })
}
