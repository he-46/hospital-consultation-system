import request from './index'

// 获取疾病列表
export function getDiseaseList(params) {
  return request({
    url: '/disease/list',
    method: 'get',
    params
  })
}

// 获取热门疾病
export function getHotDiseases(limit = 8) {
  return request({
    url: '/disease/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取疾病详情
export function getDiseaseDetail(id) {
  return request({
    url: '/disease/detail',
    method: 'get',
    params: { id }
  })
}

// 搜索疾病
export function searchDiseases(params) {
  return request({
    url: '/disease/search',
    method: 'get',
    params
  })
}
