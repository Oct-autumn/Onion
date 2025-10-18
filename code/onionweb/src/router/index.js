import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../components/layouts/MainLayout.vue'
import ProjectView from '../views/ProjectView.vue'
import UserView from '../views/UserView.vue'
import ProfileView from '../views/ProfileView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProjectDetailLayout from '../views/project/ProjectDetailLayout.vue'
import ProjectInfoView from '../views/project/ProjectInfoView.vue'
import KanbanView from '../views/project/KanbanView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/project',
    meta: { requiresAuth: true },
    children: [
      { path: '/project', name: 'project', component: ProjectView },
      { path: '/user', name: 'user', component: UserView },
      { path: '/profile', name: 'profile', component: ProfileView },
      {
        path: '/project/:id',
        component: ProjectDetailLayout,
        props: true,
        children: [
          { path: '', redirect: 'info' },
          { path: 'info', name: 'project-info', component: ProjectInfoView },
          { path: 'kanban', name: 'project-kanban', component: KanbanView },
        ],
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token')
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
