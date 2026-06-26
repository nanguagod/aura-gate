<template>
  <div class="app-container">
    <!-- 顶部查询 -->
    <el-form :model="query" ref="queryRef" label-width="70px" inline>
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="query.roleName" placeholder="请输入角色名称"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 顶部按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleInsert">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button :disabled="single" type="success" plain icon="Edit" @click="handleUpdate">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button :disabled="multiple" type="danger" plain icon="Delete" @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-table :data="roleList" style="width: 100%" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column prop="roleId" label="角色编号" width="180" align="center"/>
      <el-table-column prop="roleName" label="角色名称" align="center"/>
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <pagination :total="total"
                v-model:page="query.pageNum"
                v-model:limit="query.pageSize"
                @pagination="getList"
    />

    <!-- 添加或修改角色对话框 -->
    <vxe-modal :title="title" v-model="open" width="500px" showFooter show-maximize resize>
      <el-form ref="roleRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称"/>
        </el-form-item>
        <el-form-item label="角色顺序" prop="roleSort">
          <el-input v-model="form.roleSort" placeholder="请输入角色顺序"/>
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
              style="width: 100%"
              :data="menuOptions"
              show-checkbox
              default-expand-all
              ref="menuRef"
              node-key="id"
              :props="{ label: 'label', children: 'children' }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="open = false">取消</el-button>
        </div>
      </template>
    </vxe-modal>

  </div>
</template>

<script setup>
import {nextTick, onMounted, ref} from "vue";
import {deleteRoleByRoleIds, insertRole, selectRoleByRoleId, selectRoleList, updateRole} from "@/api/system/role.js";
import Pagination from "@/components/Pagination/index.vue";
import {VxeModal} from "vxe-pc-ui";
import {ElMessage, ElMessageBox} from "element-plus";
import {selectRoleMenusTree, selectRoleMenuTree} from "@/api/system/menu.js";

//菜单权限表单实例
const menuRef = ref()

//表单实例
const roleRef = ref()

//对话框title
const title = ref('')

//对话框是否打开
const open = ref(false)

//表单参数
const form = ref({
  roleId: null,
  roleName: null,
  roleSort: null,
  menuIds: []
})

//表单校验
const rules = ref({
  roleName: [
    {required: true, message: '请输入角色名称', trigger: 'blur'}
  ],
  roleSort: [
    {required: true, message: '请输入角色顺序', trigger: 'blur'}
  ],
})

//新增按钮
const handleInsert = () => {
  if (menuRef.value) {
    menuRef.value.setCheckedKeys([])
  }
  form.value = {
    roleId: null,
    roleName: null,
    roleSort: null,
    menuIds: []
  }
  selectRoleMenusTree().then(res => {
    menuOptions.value = res.data
    open.value = true
    title.value = '新增角色'
  })
}

//修改按钮
const handleUpdate = (row) => {
  if (menuRef.value) {
    menuRef.value.setCheckedKeys([])
  }
  form.value = {
    roleId: null,
    roleName: null,
    roleSort: null,
    menuIds: []
  }
  const roleId = row.roleId || ids.value

  //根据角色ID查询对应菜单树
  const roleMenu = getRoleMenuTreeSelect(roleId);
  selectRoleByRoleId(roleId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改角色'

    //等待DOM更新完成后执行菜单选中操作
    nextTick(() => {
      //等待菜单树数据加载完成
      roleMenu.then((res) => {
        //获取该角色已选中的菜单key数组
        let checkedKeys = res.checkedKeys
        //遍历所有已选中的菜单key, 在菜单树组件中设置选中状态
        checkedKeys.forEach((v) => {
          //针对每个菜单ID, 再次使用nextTick确保菜单树组件完全准备好
          nextTick(() => {
            menuRef.value.setChecked(v, true, false)
          })
        })

      })
    })

  })
}

//菜单树数据
const menuOptions = ref([])

//根据角色ID查询对应菜单树
const getRoleMenuTreeSelect = (roleId) => {
  return selectRoleMenuTree(roleId).then(res => {
    //重置菜单选项前先清空数组, 防止重复数据
    menuOptions.value = []
    //然后再赋值新的菜单树数据
    menuOptions.value = res.menus
    return res
  })
}

//删除按钮
const handleDelete = (row) => {
  console.log(ids.value, '看看ID数组')
  const roleIds = row.roleId || ids.value
  ElMessageBox.confirm(
      '是否确认删除角色?',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        //调用删除api
        deleteRoleByRoleIds(roleIds).then(res => {
          ElMessage.success('删除成功')
          getList()
        })
      })
}

//保存按钮
const submitForm = () => {
  roleRef.value.validate(valid => {
    if (valid) {
      if (form.value.roleId != null) {
        form.value.menuIds = getMenuAllCheckedKeys()
        //调用修改api
        updateRole(form.value).then(res => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        //调用新增api
        form.value.menuIds = getMenuAllCheckedKeys()
        insertRole(form.value).then(res => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

//获取菜单组件中所有被选中的节点
const getMenuAllCheckedKeys = () => {
  //全选中的菜单节点
  let checkedKeys = menuRef.value.getCheckedKeys()
  //半选中的菜单节点
  let halfCheckedKeys = menuRef.value.getHalfCheckedKeys()
  //将半选中的节点合并到已选中的节点列表中
  checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)
  //返回合并后的所有选中节点ID数组
  return checkedKeys
}

//已勾选的id数组
const ids = ref([])

//当前是否未选中单行
const single = ref(true)

//当前是否未选中多行
const multiple = ref(true)

//顶部查询表单实例
const queryRef = ref()

//多选时的触发方法
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.roleId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

//查询参数
const query = ref({
  pageNum: 1,
  pageSize: 10,
  roleName: null,
})

//角色列表数据
const roleList = ref([])

//数据总数
const total = ref(0)

//搜索按钮
const handleQuery = () => {
  query.value.pageNum = 1
  getList()
}

//重置按钮
const resetQuery = () => {
  queryRef.value.resetFields()
  handleQuery()
}

//查询数据
const getList = () => {
  selectRoleList(query.value).then(res => {
    roleList.value = res.rows
    total.value = res.total
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>

</style>
