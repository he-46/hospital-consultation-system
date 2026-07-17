import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue')
  },
  {
    path: '/hospital',
    name: 'Hospital',
    component: () => import('@/views/Hospital.vue')
  },
  {
    path: '/hospital/:id',
    name: 'HospitalDetail',
    component: () => import('@/views/HospitalDetail.vue')
  },
  {
    path: '/doctor',
    name: 'Doctor',
    component: () => import('@/views/Doctor.vue')
  },
  {
    path: '/doctor/:id',
    name: 'DoctorDetail',
    component: () => import('@/views/DoctorDetail.vue')
  },
  {
    path: '/reservation/:doctorId',
    name: 'Reservation',
    component: () => import('@/views/Reservation.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reservation-pay/:id',
    name: 'ReservationPay',
    component: () => import('@/views/ReservationPay.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reservation-success/:id',
    name: 'ReservationSuccess',
    component: () => import('@/views/ReservationSuccess.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/article',
    name: 'Article',
    component: () => import('@/views/Article.vue')
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetail.vue')
  },
  {
    path: '/disease',
    name: 'Disease',
    component: () => import('@/views/Disease.vue')
  },
  {
    path: '/disease/:id',
    name: 'DiseaseDetail',
    component: () => import('@/views/DiseaseDetail.vue')
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue')
  },
  {
    path: '/personal',
    name: 'Personal',
    component: () => import('@/views/Personal.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: () => import('@/views/Feedback.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/Messages.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
