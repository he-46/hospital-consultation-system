import request from './index'

// 获取首页聚合数据（推荐医院+医生+疾病+文章）
export const getIndexData = () => {
  return request.get('/index/data')
}
