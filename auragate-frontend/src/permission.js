import router from '@/router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const whiteList = ['/login', '/register']

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const hasToken = !!userStore.token

  if (hasToken) {
    if (to.path === '/login') {
      next('/dashboard')
    } else {
      // Fetch user info if not yet loaded
      if (!userStore.userInfo) {
        try {
          await userStore.fetchUserInfo()
          await userStore.fetchMenus()
        } catch {
          await userStore.logout()
          next('/login')
          return
        }
      }
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      ElMessage.warning('请先登录')
      next('/login')
    }
  }
})
