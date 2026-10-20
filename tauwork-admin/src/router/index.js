import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/login.vue') },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/dashboard.vue'), meta: { title: '经营驾驶舱' } },
      { path: 'bookings', component: () => import('../views/bookings.vue'), meta: { title: '预订订单' } },
      { path: 'contracts', component: () => import('../views/contracts.vue'), meta: { title: '合同订单' } },
      { path: 'svcbookings', redirect: '/appointments' },
      { path: 'customers', component: () => import('../views/customers.vue'), meta: { title: '客户库' } },
      { path: 'leads', component: () => import('../views/leads.vue'), meta: { title: '线索池' } },
      { path: 'appointments', component: () => import('../views/appointments.vue'), meta: { title: '预约登记' } },
      { path: 'follows', component: () => import('../views/follows.vue'), meta: { title: '跟进日志' } },
      { path: 'goods', component: () => import('../views/product-desk.vue'), meta: { title: '通用商品', prodType: 'GOODS' } },
      { path: 'services', component: () => import('../views/product-desk.vue'), meta: { title: '服务项目', prodType: 'SERVICE' } },
      { path: 'space-products', component: () => import('../views/product-desk.vue'), meta: { title: '空间产品', prodType: 'SPACE' } },
      { path: 'goods-orders', component: () => import('../views/goods-orders.vue'), meta: { title: '商品订单' } },
      { path: 'products', redirect: '/services' },
      { path: 'communities', component: () => import('../views/communities.vue'), meta: { title: '社区管理' } },
      { path: 'consultants', component: () => import('../views/consultants.vue'), meta: { title: '顾问管理' } },
      { path: 'work-orders', component: () => import('../views/work-orders.vue'), meta: { title: '工单管理' } },
      { path: 'deposits', component: () => import('../views/deposits.vue'), meta: { title: '定金与押金' } },
      { path: 'receivables', component: () => import('../views/receivables.vue'), meta: { title: '应收账单' } },
      { path: 'settlements', component: () => import('../views/settlements.vue'), meta: { title: '收入结算' } },
      { path: 'cms', redirect: '/cms/banner' },
      { path: 'cms/banner', component: () => import('../views/cms.vue'), meta: { title: '广告展示', contentType: 'BANNER' } },
      { path: 'cms/story', component: () => import('../views/cms.vue'), meta: { title: '客户故事', contentType: 'CASE_STORY' } },
      { path: 'cms/news', component: () => import('../views/cms.vue'), meta: { title: '公告资讯', contentType: 'NEWS' } },
      { path: 'partners', component: () => import('../views/partners.vue'), meta: { title: '合伙人列表' } },
      { path: 'partner-rules', component: () => import('../views/partner-rules.vue'), meta: { title: '提成规则' } },
      { path: 'system/users', component: () => import('../views/system-users.vue'), meta: { title: '用户管理' } },
      { path: 'system/roles', component: () => import('../views/system-roles.vue'), meta: { title: '角色权限' } }
    ]
  }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach(to => {
  if (to.path === '/login') return true
  if (!localStorage.getItem('token')) return '/login'
  let user = {}
  try { user = JSON.parse(localStorage.getItem('user') || '{}') } catch (e) { user = {} }
  const allowed = []
  const walk = nodes => (nodes || []).forEach(node => {
    if (node.path) allowed.push(node.path)
    walk(node.children)
  })
  walk(user.menus)
  if (allowed.length && to.path !== '/' && !allowed.includes(to.path)) return allowed[0]
})

export default router
