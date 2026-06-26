import request from '@/utils/request'

/** 登录 */
export function login(username, password) {
  return request.post('/login', { userName: username, password })
}

/** 注册 */
export function register(username, password) {
  return request.post('/register', { userName: username, password })
}

/** 获取用户信息 */
export function getInfo() {
  return request.get('/getInfo')
}

/** 获取菜单路由 */
export function getRouters() {
  return request.get('/system/menu/getRouters')
}

/** 退出登录 */
export function logout() {
  return request.post('/logout')
}
