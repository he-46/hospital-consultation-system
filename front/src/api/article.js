import request from './index'

// 获取文章列表
export function getArticleList(params) {
  return request({
    url: '/article/list',
    method: 'get',
    params
  })
}

// 获取热门文章
export function getHotArticles(limit = 5) {
  return request({
    url: '/article/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取文章详情
export function getArticleDetail(id) {
  return request({
    url: '/article/detail',
    method: 'get',
    params: { id }
  })
}

// 搜索文章
export function searchArticles(params) {
  return request({
    url: '/article/search',
    method: 'get',
    params
  })
}
