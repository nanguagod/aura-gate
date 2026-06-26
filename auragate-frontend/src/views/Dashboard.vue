<template>
  <div>
    <h2 style="margin-bottom: 20px;">仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover" style="margin-bottom: 20px;">
          <div style="display: flex; align-items: center; gap: 16px;">
            <el-icon :size="40" :color="card.color">
              <component :is="card.icon" />
            </el-icon>
            <div>
              <div style="font-size: 28px; font-weight: bold;">{{ card.value }}</div>
              <div style="color: #999; font-size: 14px;">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header><span>系统状态</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="后端服务">AuraGate · 8080</el-descriptions-item>
            <el-descriptions-item label="AI 模型">qwen-plus</el-descriptions-item>
            <el-descriptions-item label="向量数据库">PGVector</el-descriptions-item>
            <el-descriptions-item label="搜索引擎">Elasticsearch</el-descriptions-item>
            <el-descriptions-item label="缓存">Redis</el-descriptions-item>
            <el-descriptions-item label="消息队列">RabbitMQ</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>快速入口</span></template>
          <el-space direction="vertical" fill style="width: 100%;">
            <el-button type="primary" @click="$router.push('/ai/agent')" icon="MagicStick">AI 智能体对话</el-button>
            <el-button type="success" @click="$router.push('/knowledge/qa')" icon="ChatDotRound">知识库问答</el-button>
            <el-button @click="$router.push('/knowledge/docs')" icon="FolderOpened">文档管理</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const cards = [
  { title: '用户数', value: 1, icon: 'User', color: '#409eff' },
  { title: '菜单数', value: 4, icon: 'Menu', color: '#67c23a' },
  { title: 'AI 对话', value: '-', icon: 'ChatDotSquare', color: '#e6a23c' },
  { title: '知识文档', value: '-', icon: 'Document', color: '#f56c6c' },
]
</script>
