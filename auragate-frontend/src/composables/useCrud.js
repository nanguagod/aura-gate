import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 通用 CRUD composable — 封装列表页的新增/编辑/删除/提交/表单重置逻辑
 *
 * @param {Object}   opts
 * @param {string}   opts.itemName       - 实体中文名（如"用户"、"角色"）
 * @param {Function} opts.listApi        - 列表查询 API
 * @param {Function} opts.addApi         - 新增 API
 * @param {Function} opts.updateApi      - 修改 API
 * @param {Function} opts.delApi         - 删除 API（接收 id）
 * @param {Function} opts.defaultForm    - 返回默认表单对象的工厂函数
 * @param {Object}   opts.rules          - el-form 校验规则
 * @param {Function} [opts.getId]        - 从行数据取 id
 * @param {Function} [opts.mapRowToForm] - 编辑时将行数据映射为表单对象
 * @param {Function} [opts.customFetch]  - 自定义数据获取函数（如需要额外处理如构建树）
 */
export function useCrud({
  itemName,
  listApi,
  addApi,
  updateApi,
  delApi,
  defaultForm,
  rules,
  getId = null,
  mapRowToForm = null,
  customFetch = null
}) {
  const items = ref([])
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const submitting = ref(false)
  const formRef = ref(null)

  const form = ref(defaultForm())

  // --------------- helpers ---------------
  const resolveId = (row) => {
    if (getId) return getId(row)
    // auto-detect: 找第一个以 Id/id 结尾的 key
    const key = Object.keys(row).find(k => /id$/i.test(k))
    return key ? row[key] : row.id
  }

  const resolveFormId = () => {
    const f = form.value
    const key = Object.keys(f).find(k => /id$/i.test(k))
    return key ? f[key] : f.id
  }

  // --------------- core methods ---------------
  async function fetchItems() {
    try {
      const res = await listApi()
      if (res.code === 200) {
        items.value = res.rows || res.data || []
      }
    } catch {
      // handled by interceptor
    }
  }

  function handleAdd() {
    isEdit.value = false
    dialogVisible.value = true
  }

  function handleEdit(row) {
    isEdit.value = true
    if (mapRowToForm) {
      form.value = mapRowToForm(row)
    } else {
      const defaults = defaultForm()
      form.value = { ...defaults, ...row }
    }
    dialogVisible.value = true
  }

  function handleDelete(row) {
    const name = row.menuName || row.roleName || row.userName || row.name || ''
    ElMessageBox.confirm(`确定删除${itemName}「${name}」吗？`, '删除确认', {
      type: 'warning'
    }).then(async () => {
      const res = await delApi(resolveId(row))
      if (res.code === 200) {
        ElMessage.success('删除成功')
        doFetch()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    }).catch(() => {})
  }

  async function handleSubmit() {
    try {
      const valid = await formRef.value.validate()
      if (!valid) return
    } catch {
      return
    }
    submitting.value = true
    try {
      const api = isEdit.value ? updateApi : addApi
      const res = await api(form.value)
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
        dialogVisible.value = false
        doFetch()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } finally {
      submitting.value = false
    }
  }

  function resetForm() {
    form.value = defaultForm()
    formRef.value?.resetFields()
  }

  // 使用自定义 fetch 则覆盖
  const doFetch = customFetch || fetchItems

  // --------------- lifecycle ---------------
  onMounted(doFetch)

  return {
    // state
    items,
    dialogVisible,
    isEdit,
    submitting,
    formRef,
    form,
    rules,
    // methods
    fetchItems,
    handleAdd,
    handleEdit,
    handleDelete,
    handleSubmit,
    resetForm
  }
}
