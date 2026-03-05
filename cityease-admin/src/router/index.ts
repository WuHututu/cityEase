import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '@/views/layout/index.vue'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { title: '登录 - CityEase' }
    },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard', // 访问根目录时，自动重定向到数据大屏
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                // 动态导入大屏组件
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '数据大屏' }
            },
            {
                path: 'repair',
                name: 'Repair',
                // 顺手把报修工单的路由也配上
                component: () => import('@/views/repair/index.vue'),
                meta: { title: '报修工单' }
            },
            {
                path: 'house',
                name: 'House',
                component: () => import('@/views/house/index.vue'),
                meta: { title: '房屋管理' }
            }
        ]
    },
    {
        // 捕获所有未匹配的路由，跳转到大屏或 404 (这里简单处理为回首页)
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局前置路由守卫
router.beforeEach((to, _from, next) => {
    // 动态修改浏览器标签页标题
    if (to.meta.title) {
        document.title = to.meta.title as string
    }

    // 校验登录状态
    const token = localStorage.getItem('satoken')

    if (to.path !== '/login' && !token) {
        // 如果没有 Token 且访问的不是登录页，强制打回登录页
        next('/login')
    } else if (to.path === '/login' && token) {
        // 如果已经登录了还要去登录页，直接送他进大屏
        next('/')
    } else {
        // 正常放行
        next()
    }
})

export default router