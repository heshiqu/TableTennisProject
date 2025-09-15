import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/register',
    component: () => import('@/views/register/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: { title: 'Dashboard', icon: 'dashboard' }
    }]
  },



  {
    path: '/student-dashboard',
    component: Layout,
    name: 'StudentDashboard',
    meta: { title: '个人首页', icon: 'dashboard', roles: ['STUDENT'] },
    children: [{
      path: '/student-dashboard',
      component: () => import('@/views/student/dashboard'),
      meta: { title: '个人首页', icon: 'dashboard' }
    }]
  },
  {
    path: '/student-profile',
    component: Layout,
    name: 'StudentProfile',
    meta: { title: '个人信息', icon: 'user', roles: ['STUDENT'] },
    children: [{
      path: '/student-profile',
      component: () => import('@/views/student/profile'),
      meta: { title: '个人信息', icon: 'user' }
    }]
  },
  {
    path: '/coach-search',
    component: Layout,
    name: 'CoachSearch',
    meta: { title: '查找教练', icon: 'search', roles: ['STUDENT'] },
    children: [{
      path: '',
      component: () => import('@/views/student/coach-search'),
      meta: { title: '查找教练', icon: 'search' }
    }]
  },
  {
    path: '/my-coaches',
    component: Layout,
    name: 'MyCoaches',
    meta: { title: '我的教练', icon: 'user-solid', roles: ['STUDENT'] },
    children: [{
      path: '/my-coaches',
      component: () => import('@/views/student/my-coaches'),
      meta: { title: '我的教练', icon: 'user-solid' }
    }]
  },
  {
    path: '/course-book',
    component: Layout,
    name: 'CourseBook',
    meta: { title: '预约课程', icon: 'time', roles: ['STUDENT'] },
    children: [{
      path: '/course-book',
      component: () => import('@/views/student/course-book'),
      meta: { title: '预约课程', icon: 'time' }
    }]
  },
  {
    path: '/student-schedule',
    component: Layout,
    name: 'StudentSchedule',
    meta: { title: '我的课表', icon: 'calendar', roles: ['STUDENT'] },
    children: [{
      path: '/student-schedule',
      component: () => import('@/views/course/StudentSchedule'),
      meta: { title: '我的课表', icon: 'calendar' }
    }]
  },
  {
    path: '/student-training-records',
    component: Layout,
    name: 'StudentTrainingRecords',
    meta: { title: '训练记录', icon: 'documentation', roles: ['STUDENT'] },
    children: [{
      path: '/student-training-records',
      component: () => import('@/views/student/training-records'),
      meta: { title: '训练记录', icon: 'documentation' }
    }]
  },
  {
    path: '/student-account',
    component: Layout,
    name: 'StudentAccount',
    meta: { title: '账户管理', icon: 'money', roles: ['STUDENT'] },
    children: [{
      path: '/student-account',
      component: () => import('@/views/student/account'),
      meta: { title: '账户管理', icon: 'money' }
    }]
  },
  {
    path: '/tournament',
    component: Layout,
    name: 'Tournament',
    meta: { title: '比赛报名', icon: 'trophy', roles: ['STUDENT'] },
    children: [{
      path: '/tournament',
      component: () => import('@/views/student/tournament'),
      meta: { title: '比赛报名', icon: 'trophy' }
    }]
  },
  {
    path: '/my-tournaments',
    component: Layout,
    name: 'MyTournaments',
    meta: { title: '我的比赛', icon: 'medal', roles: ['STUDENT'] },
    children: [{
      path: '/my-tournaments',
      component: () => import('@/views/student/my-tournaments'),
      meta: { title: '我的比赛', icon: 'medal' }
    }]
  },

  {
    path: '/coach-dashboard',
    component: Layout,
    name: 'CoachDashboard',
    meta: { title: '个人首页', icon: 'dashboard', roles: ['COACH'] },
    children: [{
      path: '/coach-dashboard',
      component: () => import('@/views/coach/dashboard'),
      meta: { title: '个人首页', icon: 'dashboard' }
    }]
  },
  {
    path: '/coach-profile',
    component: Layout,
    name: 'CoachProfile',
    meta: { title: '个人信息', icon: 'user', roles: ['COACH'] },
    children: [{
      path: '/coach-profile',
      component: () => import('@/views/coach/profile'),
      meta: { title: '个人信息', icon: 'user' }
    }]
  },
  {
    path: '/coach-students',
    component: Layout,
    name: 'CoachStudents',
    meta: { title: '我的学员', icon: 'peoples', roles: ['COACH'] },
    children: [{
      path: '/coach-students',
      component: () => import('@/views/coach/students'),
      meta: { title: '我的学员', icon: 'peoples' }
    }]
  },
  {
    path: '/coach-schedule',
    component: Layout,
    name: 'CoachSchedule',
    meta: { title: '课表管理', icon: 'calendar', roles: ['COACH'] },
    children: [{
      path: '/coach-schedule',
      component: () => import('@/views/course/CoachSchedule'),
      meta: { title: '课表管理', icon: 'calendar' }
    }]
  },
  {
    path: '/coach-appointments',
    component: Layout,
    name: 'CoachAppointments',
    meta: { title: '预约审核', icon: 'time', roles: ['COACH'] },
    children: [{
      path: '/coach-appointments',
      component: () => import('@/views/coach/appointments'),
      meta: { title: '预约审核', icon: 'time' }
    }]
  },
  {
    path: '/coach-training-records',
    component: Layout,
    name: 'TrainingRecords',
    meta: { title: '训练记录', icon: 'documentation', roles: ['COACH'] },
    children: [{
      path: '/coach-training-records',
      component: () => import('@/views/coach/training-records'),
      meta: { title: '训练记录', icon: 'documentation' }
    }]
  },


  {
    path: '/super-admin-dashboard',
    component: Layout,
    name: 'SuperAdminDashboard',
    meta: { title: '超级管理首页', icon: 'dashboard', roles: ['SUPER_ADMIN'] },
    children: [{
      path: '/super-admin-dashboard',
      component: () => import('@/views/super-admin/dashboard'),
      meta: { title: '超级管理首页', icon: 'dashboard' }
    }]
  },
  {
    path: '/campus-manage',
    component: Layout,
    name: 'CampusManage',
    meta: { title: '校区管理', icon: 'building', roles: ['SUPER_ADMIN'] },
    children: [{
      path: '/campus-manage',
      component: () => import('@/views/super-admin/campus-management'),
      meta: { title: '校区管理', icon: 'building' }
    }]
  },
  {
    path: '/campus-admin-manage',
    component: Layout,
    name: 'CampusAdminManage',
    meta: { title: '校区管理员', icon: 'user-solid', roles: ['SUPER_ADMIN'] },
    children: [{
      path: '/campus-admin-manage',
      component: () => import('@/views/admin/campus-admin'),
      meta: { title: '校区管理员', icon: 'user-solid' }
    }]
  },

  {
    path: '/admin-dashboard',
    component: Layout,
    name: 'AdminDashboard',
    meta: { title: '管理首页', icon: 'dashboard', roles: ['ADMIN'] },
    children: [{
      path: '/admin-dashboard',
      component: () => import('@/views/admin/dashboard'),
      meta: { title: '管理首页', icon: 'dashboard' }
    }]
  },
  {
    path: '/admin-students',
    component: Layout,
    name: 'StudentManage',
    meta: { title: '学员管理', icon: 'peoples', roles: ['ADMIN', 'SUPER_ADMIN'] },
    children: [{
      path: '/admin-students',
      component: () => import('@/views/admin/students'),
      meta: { title: '学员管理', icon: 'peoples' }
    }]
  },
  {
    path: '/admin-coaches',
    component: Layout,
    name: 'CoachManage',
    meta: { title: '教练管理', icon: 'user', roles: ['ADMIN', 'SUPER_ADMIN'] },
    children: [{
      path: '/admin-coaches',
      component: () => import('@/views/admin/coaches'),
      meta: { title: '教练管理', icon: 'user' }
    }]
  },
  {
    path: '/admin-courses',
    component: Layout,
    name: 'CourseManage',
    meta: { title: '课程管理', icon: 'education', roles: ['ADMIN', 'SUPER_ADMIN'] },
    children: [{
      path: '/admin-courses',
      component: () => import('@/views/admin/courses'),
      meta: { title: '课程管理', icon: 'education' }
    }]
  },
  {
    path: '/admin-tournaments',
    component: Layout,
    name: 'TournamentManage',
    meta: { title: '比赛管理', icon: 'trophy', roles: ['ADMIN', 'SUPER_ADMIN'] },
    children: [{
      path: '/admin-tournaments',
      component: () => import('@/views/admin/tournaments'),
      meta: { title: '比赛管理', icon: 'trophy' }
    }]
  },
  {
    path: '/admin-settings',
    component: Layout,
    name: 'SystemSettings',
    meta: { title: '系统设置', icon: 'setting', roles: ['ADMIN', 'SUPER_ADMIN'] },
    children: [{
      path: '/admin-settings',
      component: () => import('@/views/admin/settings'),
      meta: { title: '系统设置', icon: 'setting' }
    }]
  },
  {
    path: '/system-logs',
    component: Layout,
    name: 'SystemLogs',
    meta: { title: '系统日志', icon: 'log', roles: ['SUPER_ADMIN'] },
    children: [{
      path: '/system-logs',
      component: () => import('@/views/admin/system-logs'),
      meta: { title: '系统日志', icon: 'log' }
    }]
  },
  {
    path: '/license-manage',
    component: Layout,
    name: 'LicenseManage',
    meta: { title: '许可证管理', icon: 'key', roles: ['SUPER_ADMIN'] },
    children: [{
      path: '/license-manage',
      component: () => import('@/views/admin/license'),
      meta: { title: '许可证管理', icon: 'key' }
    }]
  },

  {
    path: '/campus-dashboard',
    component: Layout,
    name: 'CampusDashboard',
    meta: { title: '校区首页', icon: 'dashboard', roles: ['CAMPUS_ADMIN'] },
    children: [{
      path: '/campus-dashboard',
      component: () => import('@/views/campus/dashboard'),
      meta: { title: '校区首页', icon: 'dashboard' }
    }]
  },
  {
    path: '/campus-info',
    component: Layout,
    name: 'CampusInfo',
    meta: { title: '校区信息', icon: 'building', roles: ['CAMPUS_ADMIN'] },
    children: [{
      path: '/campus-info',
      component: () => import('@/views/campus/campus-info'),
      meta: { title: '校区信息', icon: 'building' }
    }]
  },
  {
    path: '/campus-students',
    component: Layout,
    name: 'CampusStudents',
    meta: { title: '学员管理', icon: 'peoples', roles: ['CAMPUS_ADMIN'] },
    children: [{
      path: '/campus-students',
      component: () => import('@/views/campus/students'),
      meta: { title: '学员管理', icon: 'peoples' }
    }]
  },
  {
    path: '/campus-coaches',
    component: Layout,
    name: 'CampusCoaches',
    meta: { title: '教练管理', icon: 'user', roles: ['CAMPUS_ADMIN'] },
    children: [{
      path: '/campus-coaches',
      component: () => import('@/views/campus/coaches'),
      meta: { title: '教练管理', icon: 'user' }
    }]
  },
  {
    path: '/campus-courses',
    component: Layout,
    name: 'CampusCourses',
    meta: { title: '课程管理', icon: 'education', roles: ['CAMPUS_ADMIN'] },
    children: [{
      path: '/campus-courses',
      component: () => import('@/views/campus/courses'),
      meta: { title: '课程管理', icon: 'education' }
    }]
  },
  {
    path: '/campus-appointments',
    component: Layout,
    name: 'CampusAppointments',
    meta: { title: '预约管理', icon: 'time', roles: ['CAMPUS_ADMIN'] },
    children: [{
      path: '/campus-appointments',
      component: () => import('@/views/campus/appointments'),
      meta: { title: '预约管理', icon: 'time' }
    }]
  },
  {
    path: '/campus-tournaments',
    component: Layout,
    name: 'CampusTournaments',
    meta: { title: '比赛管理', icon: 'trophy', roles: ['CAMPUS_ADMIN'] },
    children: [{
      path: '/campus-tournaments',
      component: () => import('@/views/campus/tournaments'),
      meta: { title: '比赛管理', icon: 'trophy' }
    }]
  },
  {
    path: '/campus-finance',
    component: Layout,
    name: 'CampusFinance',
    meta: { title: '财务管理', icon: 'money', roles: ['CAMPUS_ADMIN'] },
    children: [{
      path: '/campus-finance',
      component: () => import('@/views/campus/finance'),
      meta: { title: '财务管理', icon: 'money' }
    }]
  },
  {
    path: '/campus-system-logs',
    component: Layout,
    name: 'CampusSystemLogs',
    meta: { title: '操作日志', icon: 'log', roles: ['CAMPUS_ADMIN'] },
    children: [{
      path: '/campus-system-logs',
      component: () => import('@/views/campus/system-logs'),
      meta: { title: '操作日志', icon: 'log' }
    }]
  },



  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
