import { createRouter, createWebHistory } from 'vue-router'

import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import FourOFour from '@/views/404.vue'

import Home from '@/views/manager/Home.vue'
import User from '@/views/manager/User.vue'
import Person from '@/views/manager/Person.vue'
import Password from '@/views/manager/Password.vue'
import KnowledgeBase from '@/views/manager/KnowledgeBase.vue'
import KnowledgeDocument from '@/views/manager/KnowledgeDocument.vue'
import KnowledgeSegment from '@/views/manager/KnowledgeSegment.vue'
import AiModelConfig from '@/views/manager/AiModelConfig.vue'
import KnowledgeChat from '@/views/manager/KnowledgeChat.vue'

import FrontHome from '@/views/front/Home.vue'
import FrontPerson from '@/views/front/Person.vue'
import FrontPassword from '@/views/front/Password.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/front/FrontHome' },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      children: [
        { path: 'home', meta: { name: '系统首页' }, component: () => Home,  },
        { path: 'user', meta: { name: '用户信息' }, component: () => User, },
        { path: 'knowledgeBase', meta: { name: '知识库管理' }, component: () => KnowledgeBase, },
        { path: 'knowledgeDocument', meta: { name: '知识文档管理' }, component: () => KnowledgeDocument, },
        { path: 'knowledgeSegment', meta: { name: '知识片段管理' }, component: () => KnowledgeSegment, },
        { path: 'knowledgeChat', meta: { name: '智能客服问答' }, component: () => KnowledgeChat, },
        { path: 'aiModelConfig', meta: { name: '模型配置' }, component: () => AiModelConfig, },
        { path: 'person', meta: { name: '个人资料' }, component: () => Person, },
        { path: 'password', meta: { name: '修改密码' }, component: () => Password, },
      ]
    },
    {
      path: '/front',
      component: () => import('@/views/Front.vue'),
      children: [
        { path: 'FrontHome', component: () => FrontHome,  },
        { path: 'FrontPerson', component: () => FrontPerson,  },
        { path: 'FrontPassword', component: () => FrontPassword,  },
      ]
    },
    { path: '/login', component: () => Login },
    { path: '/register', component: () => Register },
    { path: '/404', component: () => FourOFour },
    { path: '/:pathMatch(.*)', redirect: '/404' }
  ]
})

// 管理端仅管理员可进：非 ADMIN 访问 /manager 一律重定向到前台首页
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('sys-user') || '{}')
  if (to.path.startsWith('/manager') && user.role !== 'ADMIN') {
    next('/front/FrontHome')
    return
  }
  next()
})

export default router
