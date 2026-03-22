import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      component: () => import('@/views/layout/index.vue'),
      children: [
        {
          path: 'home',
          name: 'Home',
          component: () => import('@/views/home/index.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'point',
          name: 'Point',
          component: () => import('@/views/point/index.vue'),
          meta: { title: '我的积分' }
        },
        {
          path: 'mall',
          name: 'Mall',
          component: () => import('@/views/mall/index.vue'),
          meta: { title: '积分商城' }
        },
        {
          path: 'repair',
          name: 'Repair',
          component: () => import('@/views/repair/index.vue'),
          meta: { title: '报修服务' }
        },
        {
          path: 'user',
          name: 'User',
          component: () => import('@/views/user/index.vue'),
          meta: { title: '我的' }
        },
        {
          path: 'user/bind-room',
          name: 'BindRoom',
          component: () => import('@/views/user/bind-room.vue'),
          meta: { title: '绑定房屋' }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('satoken')
  if (!to.meta.public && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
