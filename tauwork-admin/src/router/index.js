import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/login.vue') },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/dashboard.vue'), meta: { title: '经营驾驶舱' } },
      { path: 'bookings', component: () => import('../views/bookings.vue'), meta: { title: '空间预订订单' } },
      { path: 'products', component: () => import('../views/products.vue'), meta: { title: '产品管理' } },
      { path: 'communities', component: () => import('../views/communities.vue'), meta: { title: '社区店管理' } },
      { path: 'cms', component: () => import('../views/cms.vue'), meta: { title: '内容管理' } }
    ]
  }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach(to => {
  if (to.path !== '/login' && !localStorage.getItem('token')) return '/login'
})

export default router
