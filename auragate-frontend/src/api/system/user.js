import request from '@/utils/request'

/** 用户列表 */
export function listUser(params) {
  return request.get('/system/user/list', { params })
}

/** 新增用户 */
export function addUser(data) {
  return request.post('/system/user', data)
}

/** 修改用户 */
export function updateUser(data) {
  return request.put('/system/user', data)
}

/** 删除用户 */
export function delUser(userId) {
  return request.delete(`/system/user/${userId}`)
}
