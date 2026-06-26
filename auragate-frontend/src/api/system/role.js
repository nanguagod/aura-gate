import request from '@/utils/request'

/** 角色列表 */
export function listRole(params) {
  return request.get('/system/role/list', { params })
}

/** 新增角色 */
export function addRole(data) {
  return request.post('/system/role', data)
}

/** 修改角色 */
export function updateRole(data) {
  return request.put('/system/role', data)
}

/** 删除角色 */
export function delRole(roleId) {
  return request.delete(`/system/role/${roleId}`)
}
