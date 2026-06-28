import request from '@/utils/request'

/** 角色列表 */
export function listRole(params) {
  return request.get('/system/role/selectRoleList', { params })
}

/** 新增角色 */
export function addRole(data) {
  return request.post('/system/role/insertRole', data)
}

/** 修改角色 */
export function updateRole(data) {
  return request.put('/system/role/updateRole', data)
}

/** 删除角色 */
export function delRole(roleIds) {
  return request.delete(`/system/role/deleteRoleByRoleIds/${roleIds}`)
}
