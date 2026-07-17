import request from './index'

// 获取一级科室
export function getPrimaryDepartments() {
  return request({
    url: '/department/primary',
    method: 'get'
  })
}

// 获取二级科室
export function getSecondaryDepartments(parentId) {
  return request({
    url: '/department/secondary',
    method: 'get',
    params: { parentId }
  })
}

// 获取科室树
export function getDepartmentTree() {
  return request({
    url: '/department/tree',
    method: 'get'
  })
}
