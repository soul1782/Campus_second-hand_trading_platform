import { createRouter, createWebHistory } from 'vue-router'
import My from '../views/My.vue'
import Settings from '../views/Settings.vue'

const LoginPlaceholder = { template: '<div>Login Placeholder</div>' }
const HomePlaceholder = { template: '<div>Home Placeholder</div>' }

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPlaceholder
  },
  {
    path: '/',
    name: 'Home',
    component: HomePlaceholder,
    meta: { requiresAuth: true }
  },
  {
    path: '/my',
    name: 'My',
    component: My,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
