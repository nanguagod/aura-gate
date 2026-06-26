import request from '@/utils/request'

/** 知识库文档列表 */
export function listDocs(params) {
  return request.get('/ai/knowledge/list', { params })
}

/** 知识问答 */
export function qa(query, topK = 5) {
  return request.get('/ai/knowledge/qa', { params: { query, topK } })
}

/** 文档上传 URL（用于 el-upload action） */
export const uploadUrl = '/api/ai/knowledge/upload'

/** 获取上传请求头 */
export function uploadHeaders() {
  return { Authorization: `Bearer ${localStorage.getItem('token')}` }
}

/** 删除文档 */
export function deleteDoc(docId) {
  return request.delete(`/ai/knowledge/${docId}`)
}
