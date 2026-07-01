import request from '@/utils/request'

/** 菜单列表 */
export function listMenu(params) {
  return request.get('/system/menu/selectMenuList', { params })
}

/** 新增菜单 */
export function addMenu(data) {
  return request.post('/system/menu/insertMenu', data)
}

/** 修改菜单 */
export function updateMenu(data) {
  return request.put('/system/menu/updateMenu', data)
}

/** 删除菜单 */
export function delMenu(menuId) {
  return request.delete(`/system/menu/deleteMenuByMenuId/${menuId}`)
}
