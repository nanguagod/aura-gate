import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getInfo, getRouters, logout as logoutApi } from '@/api/login'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const menus = ref([])
  const permissions = ref([])

  async function login(username, password) {
    const res = await loginApi(username, password)
    if (res.code === 200) {
      token.value = res.token
      localStorage.setItem('token', res.token)
      // NOTE: 不在此处调用 fetchUserInfo() 和 fetchMenus()
      // 否则 userInfo 被设置后，router guard (permission.js) 会跳过 fetchMenus() 导致侧边栏为空
      // router guard 会在导航时自动检测并调用 fetchUserInfo() + fetchMenus()
    }
    return res
  }

  async function fetchUserInfo() {
    const res = await getInfo()
    if (res.code === 200) {
      userInfo.value = res.data
    }
  }

  async function fetchMenus() {
    const res = await getRouters()
    if (res.code === 200) {
      menus.value = res.data
    }
  }

  async function logout() {
    await logoutApi()
    token.value = ''
    userInfo.value = null
    menus.value = []
    localStorage.removeItem('token')
  }

  return { token, userInfo, menus, permissions, login, fetchUserInfo, fetchMenus, logout }
})
