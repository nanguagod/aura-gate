<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>用户管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增</el-button>
        </div>
      </template>
      <el-table :data="users" border stripe style="width: 100%">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="userName" label="用户名" min-width="140" />
        <el-table-column prop="nickName" label="昵称" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="form.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
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
import { listUser, addUser, updateUser, delUser } from '@/api/system/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  userId: null,
  userName: '',
  nickName: '',
  email: '',
  password: '',
  status: 0
})
const form = ref(defaultForm())

const rules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function fetchUsers() {
  const res = await listUser()
  if (res.code === 200) users.value = res.rows || []
}

function handleAdd() {
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.value = {
    userId: row.userId,
    userName: row.userName,
    nickName: row.nickName,
    email: row.email || '',
    password: '',
    status: row.status
  }
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除用户「${row.userName}」吗？`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    const res = await delUser(row.userId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchUsers()
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
    const api = isEdit.value ? updateUser : addUser
    const res = await api(form.value)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchUsers()
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

onMounted(fetchUsers)
</script>
