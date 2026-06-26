import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'DataBoard' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '个人信息', icon: 'User' },
      },
      {
        path: 'system/user',
        name: 'User',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理', icon: 'User', perms: 'system:user:list' },
      },
      {
        path: 'system/role',
        name: 'Role',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理', icon: 'Avatar', perms: 'system:role:list' },
      },
      {
        path: 'system/menu',
        name: 'Menu',
        component: () => import('@/views/system/Menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu', perms: 'system:menu:list' },
      },
      {
        path: 'ai/agent',
        name: 'AiAgent',
        component: () => import('@/views/ai/Agent.vue'),
        meta: { title: 'AI 智能体', icon: 'MagicStick' },
      },
      {
        path: 'knowledge/docs',
        name: 'KnowledgeDocs',
        component: () => import('@/views/knowledge/Docs.vue'),
        meta: { title: '知识库文档', icon: 'Document' },
      },
      {
        path: 'knowledge/qa',
        name: 'KnowledgeQa',
        component: () => import('@/views/knowledge/Qa.vue'),
        meta: { title: '知识问答', icon: 'ChatDotRound' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404', noAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
