import axios, { AxiosError } from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

// 对应后端的 ResVo 结构
export interface ResVo<T = any> {
    status: {
        code: number;
        msg: string;
    };
    result: T;
}

// 创建 axios 实例
const service: AxiosInstance = axios.create({
    // 配置跨域代理前缀，后续在 vite.config.ts 中配置
    baseURL: '/api',
    timeout: 15000
});

// 1. 请求拦截器 (Request Interceptor)
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {

        // 从 localStorage 获取登录时存入的 Sa-Token
        const token = localStorage.getItem('satoken');
        if (token && token !== 'undefined') {
            // 自动携带 Token 给后端
            config.headers['Authorization'] = token;
            // TODO 实际多余，兜底
            config.headers['satoken'] = token;
        }

        return config;
    },
    (error: AxiosError) => {
        return Promise.reject(error);
    }
);

// 2. 响应拦截器 (Response Interceptor)
service.interceptors.response.use(
    (response: AxiosResponse<ResVo>) => {
        const res = response.data;

        // 状态码为 0 代表业务处理成功 (对应后端 StatusEnum.SUCCESS)
        if (res.status.code === 0) {
            // 自动剥离外壳，组件里拿到直接就是 data 数据
            return res.result as any;
        }

        // 状态码为 401/403 代表 Token 过期或未登录
        if (res.status.code === 401 || res.status.code === 403) {
            ElMessage.warning('登录已过期，请重新登录');
            localStorage.removeItem('satoken');
            // 延迟跳转到登录页
            setTimeout(() => {
                window.location.href = '/login';
            }, 1000);
            return Promise.reject(new Error(res.status.msg));
        }

        // 其他业务异常（对应后端抛出的 CommunityException）
        ElMessage.error(res.status.msg || '系统业务异常');
        return Promise.reject(new Error(res.status.msg || 'Error'));
    },
    (error: AxiosError) => {
        // 网络崩溃或后端抛出 500 系统异常
        ElMessage.error('网络请求异常，请稍后再试');
        return Promise.reject(error);
    }
);

export default service;