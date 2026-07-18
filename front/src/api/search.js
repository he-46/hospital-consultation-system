import request from './index'

/**
 * 2-7 多类型统一搜索
 * @param {Object} params - 搜索参数
 * @param {string} params.keyword - 关键词
 * @param {string} params.type - 搜索类型: all/hospital/doctor/disease/article
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 */
export function unifiedSearch(params) {
  return request({
    url: '/search',
    method: 'get',
    params
  })
}
