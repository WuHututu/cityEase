/// <reference types="vite/client" />

declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

// 声明对 request 模块的类型
declare module '@/utils/request' {
    import { AxiosInstance } from 'axios';
    const request: AxiosInstance;
    export default request;
}