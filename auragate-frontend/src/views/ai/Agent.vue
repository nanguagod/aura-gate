<template>
  <div style="height: calc(100vh - 120px); display: flex; flex-direction: column;">
    <h2 style="margin-bottom: 16px;">🤖 AI 智能体</h2>
    <el-card style="flex: 1; display: flex; flex-direction: column; overflow: hidden;">
      <!-- Messages area -->
      <div ref="msgContainer" style="flex: 1; overflow-y: auto; padding: 16px; background: #fafafa; border-radius: 8px; margin-bottom: 16px;">
        <div v-for="(msg, idx) in messages" :key="idx" style="margin-bottom: 16px; display: flex; flex-direction: column; align-items: flex-start;">
          <div v-if="msg.role === 'user'" style="width: 100%; display: flex; justify-content: flex-end;">
            <el-tag type="primary" effect="dark" style="max-width: 70%; white-space: pre-wrap; padding: 8px 12px; font-size: 14px;">
              {{ msg.content }}
            </el-tag>
          </div>
          <div v-else style="width: 100%; display: flex;">
            <div style="max-width: 70%; background: #fff; padding: 8px 12px; border-radius: 8px; border: 1px solid #e6e6e6; white-space: pre-wrap; font-size: 14px;">
              <strong>AuraAgent: </strong>{{ msg.content }}
            </div>
          </div>
        </div>
        <!-- Streaming indicator — only when actually connected -->
        <div v-if="streaming && connectionState === 'connected'" style="display: flex; align-items: center; gap: 8px; color: #999;">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>AI 正在思考...</span>
        </div>
      </div>
      <!-- Connection status banner -->
      <div v-if="connectionState === 'connecting'" style="text-align: center; padding: 4px 0 12px 0; color: #e6a23c; font-size: 13px;">
        <el-icon class="is-loading"><Loading /></el-icon> 正在连接 AI 服务...
      </div>
      <div v-else-if="connectionState === 'error'" style="text-align: center; padding: 4px 0 12px 0; color: #f56c6c; font-size: 13px;">
        <el-icon><WarningFilled /></el-icon> 连接失败，请检查后端服务是否启动
      </div>
      <!-- Input area -->
      <div style="display: flex; gap: 12px;">
        <el-input
          v-model="input"
          placeholder="输入你的问题..."
          size="large"
          @keyup.enter="sendMessage"
          :disabled="streaming"
          clearable
        />
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
import { connectAiWebSocket } from '@/utils/websocket'

const input = ref('')
const messages = ref([
  { role: 'assistant', content: '你好！我是 AuraAgent，有什么可以帮你的吗？' },
])
const streaming = ref(false)
const connectionState = ref('idle') // 'idle' | 'connecting' | 'connected' | 'error'
const msgContainer = ref(null)
let ws = null

function sendMessage() {
  const text = input.value.trim()
  if (!text || streaming.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  streaming.value = true
  connectionState.value = 'connecting'
  scrollToBottom()

  // Add placeholder for streaming response
  messages.value.push({ role: 'assistant', content: '' })
  const lastIdx = messages.value.length - 1

  // Close previous connection if any
  if (ws) ws.close()

  ws = connectAiWebSocket(
    // onToken — 每个数据片段
    (token) => {
      messages.value[lastIdx].content += token
      scrollToBottom()
    },
    // onDone
    () => {
      streaming.value = false
      connectionState.value = 'idle'
      scrollToBottom()
    },
    // onError
    () => {
      messages.value[lastIdx].content = '连接失败，请稍后重试。\n\n提示：请确保后端已启动，且 WebSocket 端点 /ws/ai 可用。'
      streaming.value = false
      connectionState.value = 'error'
      scrollToBottom()
    },
    // onOpen — 连接建立后发送消息（注册在函数内部，避免时序竞争）
    () => {
      connectionState.value = 'connected'
      ws.send(text)
      scrollToBottom()
    },
  )
}

function clearChat() {
  if (ws) ws.close()
  messages.value = [{ role: 'assistant', content: '你好！我是 AuraAgent，有什么可以帮你的吗？' }]
  streaming.value = false
  connectionState.value = 'idle'
}

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  })
}

onUnmounted(() => {
  if (ws) ws.close()
})
</script>
