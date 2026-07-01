<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>角色管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增</el-button>
        </div>
      </template>
      <el-table :data="roles" border stripe style="width: 100%">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="roleName" label="角色名称" min-width="160" />
        <el-table-column prop="roleKey" label="权限标识" min-width="160" />
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="权限标识" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="请输入权限标识" />
        </el-form-item>
        <el-form-item label="排序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" :max="999" />
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
import { listRole, addRole, updateRole, delRole } from '@/api/system/role'
import { useCrud } from '@/composables/useCrud'

const defaultForm = () => ({
  roleId: null,
  roleName: '',
  roleKey: '',
  roleSort: 0,
  status: 0
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
}

const mapRowToForm = (row) => ({
  roleId: row.roleId,
  roleName: row.roleName,
  roleKey: row.roleKey,
  roleSort: row.roleSort || 0,
  status: row.status
})

const {
  items: roles,
  dialogVisible, isEdit, submitting, formRef, form,
  handleAdd, handleEdit, handleDelete, handleSubmit, resetForm
} = useCrud({
  itemName: '角色',
  listApi: listRole,
  addApi: addRole,
  updateApi: updateRole,
  delApi: delRole,
  defaultForm,
  rules,
  mapRowToForm
})
</script>
