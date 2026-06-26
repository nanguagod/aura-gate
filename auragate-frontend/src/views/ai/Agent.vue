<template>
  <div style="height: calc(100vh - 120px); display: flex; flex-direction: column;">
    <h2 style="margin-bottom: 16px;">🤖 AI 智能体</h2>
    <el-card style="flex: 1; display: flex; flex-direction: column; overflow: hidden;">
      <!-- Messages area -->
      <div ref="msgContainer" style="flex: 1; overflow-y: auto; padding: 16px; background: #fafafa; border-radius: 8px; margin-bottom: 16px;">
        <div v-for="(msg, idx) in messages" :key="idx" style="margin-bottom: 16px; display: flex; flex-direction: column; align-items: flex-start;">
          <div v-if="msg.role === 'user'" style="width: 100%; display: flex; justify-content: flex-end;">
            <el-tag type="primary" style="max-width: 70%; white-space: pre-wrap; padding: 8px 12px; font-size: 14px;">
              {{ msg.content }}
            </el-tag>
          </div>
          <div v-else style="width: 100%; display: flex; justify-content: flex-start;">
            <div style="max-width: 70%; background: #fff; padding: 8px 12px; border-radius: 8px; border: 1px solid #e6e6e6; white-space: pre-wrap; font-size: 14px;">
              <strong>AuraAgent: </strong>{{ msg.content }}
            </div>
          </div>
        </div>
        <!-- Streaming indicator -->
        <div v-if="streaming" style="display: flex; align-items: center; gap: 8px; color: #999;">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>AI 正在思考...</span>
        </div>
      </div>
      <!-- Input area -->
      <div style="display: flex; gap: 12px;">
        <el-input v-model="input" placeholder="输入你的问题..." size="large" @keyup.enter="sendMessage" :disabled="streaming" clearable />
        <el-button type="primary" size="large" @click="sendMessage" :disabled="streaming || !input.trim()">
          <el-icon><Promotion /></el-icon> 发送
        </el-button>
        <el-button size="large" @click="clearChat" :disabled="messages.length === 0">
          <el-icon><Delete /></el-icon> 清空
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick, onUnmounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const input = ref('')
const messages = ref([
  { role: 'assistant', content: '你好！我是 AuraAgent，有什么可以帮你的吗？' },
])
const streaming = ref(false)
const msgContainer = ref(null)
let eventSource = null

async function sendMessage() {
  const text = input.value.trim()
  if (!text || streaming.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  streaming.value = true
  scrollToBottom()

  // Add placeholder for streaming response
  messages.value.push({ role: 'assistant', content: '' })
  const lastIdx = messages.value.length - 1

  try {
    const res = await request.get('/ai/agent/chat', {
      params: { message: text },
      responseType: 'text',
      timeout: 120000,
      onDownloadProgress: (progressEvent) => {
        const data = progressEvent.event?.target?.responseText || ''
        const lines = data.split('\n')
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const content = line.substring(5)
            if (content === '[DONE]') continue
            messages.value[lastIdx].content += content
          }
        }
        scrollToBottom()
      },
    })
  } catch (e) {
    messages.value[lastIdx].content = '请求失败，请稍后重试。'
    ElMessage.error('AI 请求失败')
  } finally {
    streaming.value = false
    scrollToBottom()
  }
}

function clearChat() {
  messages.value = [{ role: 'assistant', content: '你好！我是 AuraAgent，有什么可以帮你的吗？' }]
}

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  })
}

onUnmounted(() => {
  if (eventSource) eventSource.close()
})
</script>

<style scoped>
.el-tag.el-tag--primary {
  --el-tag-text-color: #fff;
}
</style>
