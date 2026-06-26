<template>
  <div>
    <!--头部-->
    <div class="header-container">
      <div class="header">
        <div class="header-left">
          <img src="@/assets/logo/logo.png" alt="">
          <div class="titles" @click="router.push('/user/home')">前台用户端</div>
        </div>
        <div class="header-center">
          <div class="front-header-nav">
            <el-menu :default-active="route.path" mode="horizontal" :router="true">
              <el-menu-item index="/user/home">
                <el-icon>
                  <HomeFilled/>
                </el-icon>
                首页
              </el-menu-item>
              <el-menu-item index="/user/self">
                <el-icon>
                  <UserFilled/>
                </el-icon>
                个人中心
              </el-menu-item>
            </el-menu>
          </div>
        </div>
        <div class="header-right">
          <div v-if="!userStore.name" class="auth-buttons">
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button @click="router.push('/register')">注册</el-button>
          </div>
          <div v-else class="user-info">
            <el-dropdown>
              <div class="header-dropdown">
                <img :src="userStore.avatar" alt="">
                <div class="user-name">
                  <span>{{ userStore.name }}</span>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-button type="text" style="text-decoration: none" @click.native="logout">
                      退出
                    </el-button>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

    </div>

    <div>
      <router-view/>
    </div>
  </div>
</template>

<script setup>
import {useRoute, useRouter} from 'vue-router'
import {ElMessageBox} from "element-plus";
import {HomeFilled, UserFilled} from "@element-plus/icons-vue";
import useUserStore from "@/stores/modules/userStore.js";

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const logout = () => {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => {
  })
}
</script>

<style scoped>
.front-header-nav .el-menu-item {
  font-size: 16px;
  font-weight: 500;
}

.front-header-nav .el-menu--horizontal .el-menu-item:not(.is-disabled):hover {
  color: #409EFF;
  background-color: #ecf5ff;
}

.front-header-nav .el-menu--horizontal > .el-menu-item.is-active {
  color: #409EFF;
  border-bottom: 2px solid #409EFF;
}

.front-header-nav .el-menu-item .el-icon {
  margin-right: 5px;
  width: 1em;
  height: 1em;
  vertical-align: middle;
}

.header-container {
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header {
  display: flex;
  height: 70px;
  line-height: 70px;
  background-color: #409EFF;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

/* 导航菜单样式调整 */
.el-menu {
  background-color: transparent !important; /* 透明背景 */
  border-bottom: none !important;
}

.header-left {
  width: 450px;
  display: flex;
  align-items: center;
  padding-left: 30px;
}

.header-left img, .header-dropdown img {
  width: 40px;
  height: 40px;
  border-radius: 50%
}

.titles {
  color: #fff;
  cursor: pointer;
  margin-left: 15px;
  font-size: 22px;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2); /* 文字阴影 */
  transition: all 0.3s ease;
}

.titles:hover {
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.header-center {
  flex: 1;
}

.header-right {
  width: 200px;
  padding-right: 20px;
  display: flex;
  align-items: center; /* 垂直居中 */
  justify-content: flex-end; /* 右对齐 */
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: 10px; /* 按钮间距 */
}

.user-info {
  display: flex;
  align-items: center;
}

/* 用户下拉菜单 */
.header-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.header-dropdown:focus {
  outline: none;
}

.user-name {
  margin-left: 10px;
  color: #fff;
  display: flex;
  align-items: center;
}

.front-header-nav .el-menu-item.is-active {
  color: white !important;
}

.main-content {
  width: 80%;
  margin: 5px auto;
}

.el-menu.el-menu--horizontal {
  border: none !important;
  height: 59px;
}

.el-menu-item.is-active {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.2) !important;
  border-bottom: 3px solid #fff !important; /* 底部边框替代背景色 */
}

.el-menu-item {
  color: rgba(255, 255, 255, 0.9) !important;
  font-size: 16px !important;
  font-weight: 500 !important;
  padding: 0 20px !important; /* 增加间距 */
  margin: 0 !important;
  height: 70px !important;
  line-height: 70px !important;
  transition: all 0.3s ease !important; /* 平滑过渡 */
}

.el-menu-item:not(.is-active):hover {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.1) !important;
  transform: translateY(-2px); /* 悬停上浮效果 */
}

/* 按钮样式优化 */
.el-button {
  padding: 10px 20px !important;
  border-radius: 20px !important;
  font-weight: 500 !important;
  transition: all 0.3s ease !important;
}
</style>
