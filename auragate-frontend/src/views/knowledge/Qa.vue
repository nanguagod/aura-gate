<template>
  <div>
    <h2 style="margin-bottom: 20px;">💬 知识问答</h2>
    <el-card>
      <div style="display: flex; gap: 12px; margin-bottom: 16px;">
        <el-input v-model="query" placeholder="输入你想了解的问题..." size="large" @keyup.enter="search" clearable />
        <el-button type="primary" size="large" @click="search" :loading="loading" icon="Search">搜索</el-button>
      </div>
      <div v-if="answer" style="background: #f0f9eb; padding: 16px; border-radius: 8px; white-space: pre-wrap; line-height: 1.8;">
        <strong>回答：</strong>{{ answer }}
      </div>
      <el-empty v-if="!answer && !loading" description="输入问题开始搜索知识库" style="margin-top: 60px;" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const query = ref('')
const answer = ref('')
const loading = ref(false)

async function search() {
  if (!query.value.trim()) return
  loading.value = true
  answer.value = ''
  try {
    const res = await request.get('/ai/knowledge/qa', {
      params: { query: query.value, topK: 5 },
    })
    answer.value = res.answer || '未找到相关内容'
  } catch {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}
</script>
