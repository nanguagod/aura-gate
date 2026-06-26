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
      await fetchUserInfo()
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
