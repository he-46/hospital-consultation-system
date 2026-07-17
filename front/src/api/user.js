import request from './index'

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/changePassword',
    method: 'post',
    data
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'post',
    data
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 验证手机号（忘记密码）
export function verifyPhone(phone) {
  return request({
    url: '/user/verifyPhone',
    method: 'post',
    data: { phone }
  })
}

// 发送验证码
export function sendCode(phone) {
  return request({
    url: '/user/sendCode',
    method: 'post',
    data: { phone }
  })
}

// 验证验证码
export function checkCode(data) {
  return request({
    url: '/user/checkCode',
    method: 'post',
    data
  })
}

// 重置密码（忘记密码）
export function resetPassword(data) {
  return request({
    url: '/user/resetPassword',
    method: 'post',
    data
  })
}
