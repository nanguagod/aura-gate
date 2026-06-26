<template>
  <el-aside width="220px" style="background: #304156; color: #fff;">
    <div style="height: 60px; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; border-bottom: 1px solid #405468;">
      🌟 AuraGate
    </div>
    <el-menu
      :default-active="$route.path"
      router
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      style="border-right: none;"
    >
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>

      <el-sub-menu index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/system/role">
          <el-icon><Avatar /></el-icon>
          <span>角色管理</span>
        </el-menu-item>
        <el-menu-item index="/system/menu">
          <el-icon><Menu /></el-icon>
          <span>菜单管理</span>
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="ai">
        <template #title>
          <el-icon><MagicStick /></el-icon>
          <span>AI 智能体</span>
        </template>
        <el-menu-item index="/ai/agent">
          <el-icon><ChatDotSquare /></el-icon>
          <span>AI 对话</span>
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="knowledge">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>知识库</span>
        </template>
        <el-menu-item index="/knowledge/docs">
          <el-icon><FolderOpened /></el-icon>
          <span>文档管理</span>
        </el-menu-item>
        <el-menu-item index="/knowledge/qa">
          <el-icon><ChatDotRound /></el-icon>
          <span>知识问答</span>
        </el-menu-item>
      </el-sub-menu>
    </el-menu>
  </el-aside>
</template>
