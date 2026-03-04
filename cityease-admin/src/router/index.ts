import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue')
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        // 稍后我们将实现这个后台布局页面
        component: () => import('@/views/layout/index.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫：防止未登录用户访问后台页面
router.beforeEach((to, _from, next) => {
    const token = localStorage.getItem('satoken')
    if (to.path !== '/login' && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router