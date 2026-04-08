import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import AdminLayout from '../layouts/AdminLayout.vue'
import StudentLayout from '../layouts/StudentLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/LoginView.vue'),
      meta: { hideLayout: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      meta: { requiresAuth: true, role: 'ADMIN' },
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('../views/admin/DashboardView.vue')
        },
        {
          path: 'students',
          name: 'Students',
          component: () => import('../views/admin/StudentsView.vue')
        },
        {
          path: 'rooms',
          name: 'Rooms',
          component: () => import('../views/admin/RoomsView.vue')
        },
        {
          path: 'repairs',
          name: 'Repairs',
          component: () => import('../views/admin/RepairsView.vue')
        },
        {
          path: 'leaves',
          name: 'Leaves',
          component: () => import('../views/admin/LeavesView.vue')
        },
        {
          path: 'notices',
          name: 'Notices',
          component: () => import('../views/admin/NoticesView.vue')
        },
        {
          path: 'buildings',
          name: 'Buildings',
          component: () => import('../views/admin/BuildingsView.vue')
        },
        {
          path: 'admins',
          name: 'Admins',
          component: () => import('../views/admin/AdminsView.vue')
        }
      ]
    },
    {
      path: '/student',
      name: 'Student',
      component: StudentLayout,
      redirect: '/student/home',
      meta: { requiresAuth: true, role: 'STUDENT' },
      children: [
        {
          path: 'home',
          name: 'StudentHome',
          component: () => import('../views/student/HomeView.vue')
        },
        {
          path: 'repairs',
          name: 'MyRepairs',
          component: () => import('../views/student/MyRepairsView.vue')
        },
        {
          path: 'leaves',
          name: 'MyLeaves',
          component: () => import('../views/student/MyLeavesView.vue')
        },
        {
          path: 'notices',
          name: 'StudentNotices',
          component: () => import('../views/student/NoticesView.vue')
        },
        {
          path: 'profile',
          name: 'StudentProfile',
          component: () => import('../views/student/ProfileView.vue')
        }
      ]
    }
  ]
})

// 路由守卫 - 严格的鉴权逻辑
router.beforeEach((to, from, next) => {
  // 白名单路由（不需要登录）
  const whiteList = ['/login', '/']
  
  // 如果是白名单路由，直接放行
  if (whiteList.includes(to.path)) {
    next()
    return
  }
  
  // 获取用户信息
  const userStore = useUserStore()
  const hasToken = !!userStore.token
  const hasUserInfo = !!userStore.userInfo && !!userStore.role
  
  // 需要认证的页面
  if (!hasToken || !hasUserInfo) {
    // 未登录，清除数据并跳转登录
    userStore.clearAuth()
    next('/login')
    return
  }
  
  // 已登录，检查角色权限
  if (to.meta.role) {
    if (to.meta.role === userStore.role) {
      next()
    } else {
      // 角色不匹配，重定向到对应角色的首页
      console.warn(`权限不足: 需要${to.meta.role}角色，当前是${userStore.role}`)
      if (userStore.role === 'ADMIN') {
        next('/admin/dashboard')
      } else {
        next('/student/home')
      }
    }
  } else {
    next()
  }
})

export default router
