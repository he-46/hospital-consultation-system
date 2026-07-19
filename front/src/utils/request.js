import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器：自动携带登录token
service.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if(token) {
        // 后端拦截器识别 Authorization + Bearer 格式，必须这么写
        config.headers.Authorization = "Bearer " + token
    }
    return config
}, err => {
    return Promise.reject(err)
})

// 响应拦截器：统一处理返回、报错
service.interceptors.response.use(res => {
    // 直接剥离外层code包装，页面拿res.data
    return res.data
}, err => {
    // 未登录401跳登录页
    if(err.response && err.response.status === 401) {
        localStorage.removeItem('token')
        location.href = '/login'
    }
    ElMessage.error(err.response?.data?.message || '服务器请求失败')
    return Promise.reject(err)
})

export default service