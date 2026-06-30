import request from '@/utils/request'

/** Dashboard 统计数据 */
export function getDashboardStats() {
  return request.get('/dashboard/stats')
}
