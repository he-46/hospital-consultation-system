import request from './index'

// 获取系统配置
export function getConfigList() {
  return request({
    url: '/config/list',
    method: 'get'
  })
}

// 根据key获取配置
export function getConfigByKey(key) {
  return request({
    url: '/config/getByKey',
    method: 'get',
    params: { key }
  })
}
