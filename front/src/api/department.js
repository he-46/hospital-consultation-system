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

// 2-1 获取科室树（新接口别名）
export function getDepartmentTreeNew() {
  return request({
    url: '/department/departments/tree',
    method: 'get'
  })
}

// 2-2 获取某科室下的医院列表
export function getHospitalsByDepartment(departmentId, params) {
  return request({
    url: `/department/departments/${departmentId}/hospitals`,
    method: 'get',
    params
  })
}
