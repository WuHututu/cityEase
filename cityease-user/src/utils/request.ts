import axios, { type AxiosError, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { showToast } from 'vant'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('satoken')
    if (token) {
      config.headers['Authorization'] = token
      config.headers['satoken'] = token
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.status?.code === 0) {
      return res.result
    }
    if (res.status?.code === 401 || res.status?.code === 403) {
      showToast('登录已过期')
      localStorage.removeItem('satoken')
      window.location.href = '/login'
    }
    showToast(res.status?.msg || '请求失败')
    return Promise.reject(new Error(res.status?.msg || '请求失败'))
  },
  (error: AxiosError) => {
    showToast(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
