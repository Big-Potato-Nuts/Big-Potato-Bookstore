import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import pinia from '@/store'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/ReaderLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/reader/Home.vue'), meta: { title: '在线小说书城' } },
      { path: 'book', name: 'bookList', component: () => import('@/views/reader/BookList.vue'), meta: { title: '书城' } },
      { path: 'book/:id', name: 'bookDetail', component: () => import('@/views/reader/BookDetail.vue'), meta: { title: '小说详情' } },
      { path: 'read/:chapterId', name: 'reader', component: () => import('@/views/reader/Reader.vue'), meta: { title: '阅读' } },
      { path: 'bookshelf', name: 'bookshelf', component: () => import('@/views/reader/Bookshelf.vue'), meta: { title: '我的书架', requiresAuth: true } },
      { path: 'profile', name: 'profile', component: () => import('@/views/reader/Profile.vue'), meta: { title: '个人中心', requiresAuth: true } },
      { path: 'orders', name: 'orders', component: () => import('@/views/reader/Orders.vue'), meta: { title: '我的订单', requiresAuth: true } },
      { path: 'my-comments', name: 'myComments', component: () => import('@/views/reader/MyComments.vue'), meta: { title: '我的评论', requiresAuth: true } },
      { path: 'notifications', name: 'notifications', component: () => import('@/views/reader/Notifications.vue'), meta: { title: '消息通知', requiresAuth: true } },
      { path: 'appeals', name: 'myAppeals', component: () => import('@/views/reader/Appeals.vue'), meta: { title: '处罚申诉', requiresAuth: true } },
      { path: 'author-apply', name: 'authorApply', component: () => import('@/views/reader/AuthorApply.vue'), meta: { title: '作者入驻', requiresAuth: true } }
    ]
  },
  { path: '/login', name: 'login', component: () => import('@/views/reader/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'register', component: () => import('@/views/reader/Register.vue'), meta: { title: '注册' } },
  {
    path: '/author',
    component: () => import('@/layouts/AuthorLayout.vue'),
    meta: { requiresAuth: true, role: 1 },
    children: [
      { path: '', redirect: '/author/dashboard' },
      { path: 'dashboard', name: 'authorDashboard', component: () => import('@/views/author/Dashboard.vue'), meta: { title: '数据看板' } },
      { path: 'books', name: 'authorBooks', component: () => import('@/views/author/Books.vue'), meta: { title: '作品管理' } },
      { path: 'books/new', name: 'authorBookNew', component: () => import('@/views/author/BookEdit.vue'), meta: { title: '新建作品' } },
      { path: 'books/:id/edit', name: 'authorBookEdit', component: () => import('@/views/author/BookEdit.vue'), meta: { title: '编辑作品' } },
      { path: 'books/:id/chapters', name: 'authorChapters', component: () => import('@/views/author/Chapters.vue'), meta: { title: '章节管理' } },
      { path: 'books/:id/chapters/new', name: 'authorChapterNew', component: () => import('@/views/author/ChapterEdit.vue'), meta: { title: '新建章节' } },
      { path: 'chapters/:id/edit', name: 'authorChapterEdit', component: () => import('@/views/author/ChapterEdit.vue'), meta: { title: '编辑章节' } }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 2 },
    children: [
      { path: '', redirect: '/admin/users' },
      { path: 'users', name: 'adminUsers', component: () => import('@/views/admin/Users.vue'), meta: { title: '用户管理' } },
      { path: 'authors', name: 'adminAuthors', component: () => import('@/views/admin/Authors.vue'), meta: { title: '作者审核' } },
      { path: 'books', name: 'adminBooks', component: () => import('@/views/admin/Books.vue'), meta: { title: '小说管理' } },
      { path: 'book-audit', name: 'adminBookAudit', component: () => import('@/views/admin/BookAudit.vue'), meta: { title: '书籍审核' } },
      { path: 'categories', name: 'adminCategories', component: () => import('@/views/admin/Categories.vue'), meta: { title: '分类管理' } },
      { path: 'orders', name: 'adminOrders', component: () => import('@/views/admin/Orders.vue'), meta: { title: '订单管理' } },
      { path: 'comments', name: 'adminComments', component: () => import('@/views/admin/Comments.vue'), meta: { title: '评论管理' } },
      { path: 'appeals', name: 'adminAppeals', component: () => import('@/views/admin/Appeals.vue'), meta: { title: '用户申诉' } }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'notFound', component: () => import('@/views/error/NotFound.vue'), meta: { title: '页面不存在' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  const userStore = useUserStore(pinia)

  // 需要登录
  if (to.meta.requiresAuth && !userStore.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 管理员只能访问管理后台，不能进入读者前台或作者中心
  if (userStore.role === 2 && !to.path.startsWith('/admin') && to.path !== '/login' && to.path !== '/register' && to.name !== 'notFound') {
    return '/admin/users'
  }

  // 角色校验
  if (to.meta.role !== undefined && userStore.role !== to.meta.role) {
    ElMessage.warning('您没有权限访问该页面')
    if (userStore.role === 2) return '/admin/users'
    if (userStore.role === 1) return '/author/dashboard'
    return '/'
  }

  // 已登录访问登录/注册页 → 回首页（管理员回管理后台）
  if ((to.path === '/login' || to.path === '/register') && userStore.isLogin) {
    if (userStore.role === 2) return '/admin/users'
    return '/'
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · 在线小说书城` : '在线小说书城'
})

export default router
