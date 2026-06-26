<template>
  <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; background: #f0f2f5;">
    <h1 style="font-size: 120px; color: #409eff; margin: 0; line-height: 1;">404</h1>
    <p style="font-size: 20px; color: #999; margin: 20px 0 40px;">页面不存在</p>
    <el-button type="primary" size="large" @click="$router.push('/dashboard')">返回首页</el-button>
  </div>
</template>
