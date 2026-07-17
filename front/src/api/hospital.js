import request from './index'

// 获取医院列表
export function getHospitalList(params) {
  return request({
    url: '/hospital/list',
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

// 获取医院详情
export function getHospitalDetail(id) {
  return request({
    url: '/hospital/detail',
    method: 'get',
    params: { id }
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
