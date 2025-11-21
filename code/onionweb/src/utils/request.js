// src/utils/request.js
import axios from 'axios'
import router from "@/router/index.js";

// 创建 axios 实例
const service = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)


// 响应拦截器：统一处理结果和错误
service.interceptors.response.use(
    response => response.data, // 直接返回响应体（简化业务代码）
    error => {
        // 处理 401（Token 过期/无效）：清除 Token 并跳转登录页
        if (error.response?.status === 401) {
            localStorage.removeItem('token'); // 清除无效 Token
            router.push({ path: '/login', query: { redirect: router.currentRoute.fullPath } }); // 携带当前路径，登录后返回
        }
        // 其他错误：返回错误信息（业务代码可捕获）
        return Promise.reject(error.response?.data || { message: '请求失败' });
    }
);

export default service
