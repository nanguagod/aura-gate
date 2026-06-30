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
      <!-- 仪表盘始终在第一位 -->
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>

      <!-- 动态菜单：从后端 getRouters() 读取 -->
      <template v-for="menu in dynamicMenus" :key="menu.path">
        <!-- 有子菜单 → 渲染为分组 -->
        <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.path">
          <template #title>
            <el-icon v-if="menu.meta?.icon"><component :is="menu.meta.icon" /></el-icon>
            <span>{{ menu.meta?.title || menu.name }}</span>
          </template>
          <el-menu-item
            v-for="child in menu.children"
            :key="child.path"
            :index="child.path"
          >
            <el-icon v-if="child.meta?.icon"><component :is="child.meta.icon" /></el-icon>
            <span>{{ child.meta?.title || child.name }}</span>
          </el-menu-item>
        </el-sub-menu>
        <!-- 无子菜单 → 直接渲染为菜单项 -->
        <el-menu-item v-else :index="menu.path">
          <el-icon v-if="menu.meta?.icon"><component :is="menu.meta.icon" /></el-icon>
          <span>{{ menu.meta?.title || menu.name }}</span>
        </el-menu-item>
      </template>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()

// 从 store 读取后端动态菜单；若未加载则显示空避免重复
const dynamicMenus = computed(() => {
  return userStore.menus || []
})
</script>
