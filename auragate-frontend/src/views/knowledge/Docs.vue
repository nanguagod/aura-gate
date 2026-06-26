<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2>📄 知识库文档</h2>
      <el-upload
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="onUploadSuccess"
        :on-error="onUploadError"
        :show-file-list="false"
        accept=".md,.txt,.pdf"
      >
        <el-button type="primary" icon="UploadFilled">上传文档</el-button>
      </el-upload>
    </div>
    <el-table :data="documents" border stripe style="width: 100%">
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="chunks" label="分块数" width="100" align="center" />
      <el-table-column prop="uploadTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="150" align="center">
        <template #default>
          <el-button size="small" type="danger" text>删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="documents.length === 0" description="暂无文档，点击右上角上传" style="margin-top: 60px;" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const uploadUrl = '/api/ai/knowledge/upload'
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`,
}
const documents = ref([])

function onUploadSuccess(res) {
  ElMessage.success('文档上传成功')
  documents.value.unshift(res)
}

function onUploadError() {
  ElMessage.error('上传失败')
}
</script>
