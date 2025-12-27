import axios from 'axios'
import { showToast } from 'vant'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/tongshiba-api', // 后端 API 基础路径
    timeout: 30000 // 30秒超时，适应图片上传等慢请求
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 可在此添加企业微信用户标识等
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            // 如果配置了 silentError，不显示 toast
            if (!response.config.silentError) {
                showToast(res.msg || '请求失败')
            }
            return Promise.reject(new Error(res.msg || '请求失败'))
        }
        return res
    },
    error => {
        // 如果配置了 silentError，不显示 toast
        if (!error.config?.silentError) {
            showToast(error.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default request
