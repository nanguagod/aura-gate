import request from '@/utils/request'

/** 用户列表 */
export function listUser(params) {
  return request.get('/system/user/selectUserList', { params })
}

/** 新增用户 */
export function addUser(data) {
  return request.post('/system/user/insertUser', data)
}

/** 修改用户 */
export function updateUser(data) {
  return request.put('/system/user/updateUser', data)
}

/** 删除用户 */
export function delUser(userIds) {
  return request.delete(`/system/user/deleteUserByUserIds/${userIds}`)
}
