<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>菜单管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增</el-button>
        </div>
      </template>
      <el-table :data="menus" border stripe row-key="menuId" lazy style="width: 100%">
        <el-table-column prop="menuName" label="菜单名称" min-width="200" />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80" align="center" />
        <el-table-column prop="perms" label="权限标识" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="550px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTree"
            :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
            check-strictly
            clearable
            placeholder="无（顶级菜单）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 DataBoard" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="如 /system/user" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.perms" placeholder="如 system:user:list" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.orderNum" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="0" :inactive-value="1" active-text="正常" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listMenu, addMenu, updateMenu, delMenu } from '@/api/system/menu'
import { ElMessage, ElMessageBox } from 'element-plus'

const menus = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  menuId: null,
  parentId: 0,
  menuName: '',
  icon: '',
  path: '',
  perms: '',
  orderNum: 0,
  status: 0
})
const form = ref(defaultForm())

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由路径', trigger: 'blur' }]
}

// 构建菜单树（供上级菜单选择器使用）
const menuTree = ref([])

async function fetchMenus() {
  const res = await listMenu()
  if (res.code === 200) {
    menus.value = res.data || []
    // 构建树形结构供上级菜单选择
    const data = res.data || []
    menuTree.value = buildTree(data, 0)
  }
}

function buildTree(list, parentId) {
  return list
    .filter(item => item.parentId === parentId)
    .map(item => ({
      ...item,
      children: buildTree(list, item.menuId)
    }))
}

function handleAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.value = {
    menuId: row.menuId,
    parentId: row.parentId || 0,
    menuName: row.menuName,
    icon: row.icon || '',
    path: row.path || '',
    perms: row.perms || '',
    orderNum: row.orderNum || 0,
    status: row.status
  }
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除菜单「${row.menuName}」吗？`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    const res = await delMenu(row.menuId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchMenus()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  }).catch(() => {})
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const api = isEdit.value ? updateMenu : addMenu
    const res = await api(form.value)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchMenus()
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

onMounted(fetchMenus)
</script>
