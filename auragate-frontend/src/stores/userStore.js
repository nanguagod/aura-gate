import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const menus = ref([])
  const permissions = ref([])

  async function login(username, password) {
    const res = await request.post('/login', { userName: username, password })
    if (res.code === 200) {
      token.value = res.token
      localStorage.setItem('token', res.token)
      await fetchUserInfo()
    }
    return res
  }

  async function fetchUserInfo() {
    const res = await request.get('/getInfo')
    if (res.code === 200) {
      userInfo.value = res.data
    }
  }

  async function fetchMenus() {
    const res = await request.get('/system/menu/getRouters')
    if (res.code === 200) {
      menus.value = res.data
    }
  }

  async function logout() {
    await request.post('/logout')
    token.value = ''
    userInfo.value = null
    menus.value = []
    localStorage.removeItem('token')
  }

  return { token, userInfo, menus, permissions, login, fetchUserInfo, fetchMenus, logout }
})
