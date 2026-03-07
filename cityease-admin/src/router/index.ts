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
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据大屏' }
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/repair/index.vue'),
        meta: { title: '报修工单' }
      },
      {
        path: 'area',
        name: 'Area',
        component: () => import('@/views/area/index.vue'),
        meta: { title: '公共区域管理' }
      },
      {
        path: 'room',
        name: 'Room',
        component: () => import('@/views/room/index.vue'),
        meta: { title: '房屋管理' }
      },
      {
        path: 'bind',
        name: 'BindAudit',
        component: () => import('@/views/bind/index.vue'),
        meta: { title: '绑定审核' }
      },
      {
        path: 'fee',
        name: 'Fee',
        component: () => import('@/views/fee/index.vue'),
        meta: { title: '物业费账单' }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('@/views/notice/index.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'mall',
        name: 'Mall',
        component: () => import('@/views/mall/index.vue'),
        meta: { title: '积分商城' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'house',
        name: 'House',
        component: () => import('@/views/house/index.vue'),
        meta: { title: '房屋管理(旧)' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  if (to.meta.title) document.title = to.meta.title as string

  const token = localStorage.getItem('satoken')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
