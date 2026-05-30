<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="app-aside">
      <div class="logo">
        <h2>学习智能体</h2>
      </div>
      <el-menu
        :default-active="currentRoute"
        router
        class="aside-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能对话</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>学习画像</span>
        </el-menu-item>
        <el-menu-item index="/resource">
          <el-icon><Document /></el-icon>
          <span>资源生成</span>
        </el-menu-item>
        <el-menu-item index="/path">
          <el-icon><MapLocation /></el-icon>
          <span>学习路径</span>
        </el-menu-item>
        <el-menu-item index="/knowledge">
          <el-icon><Collection /></el-icon>
          <span>知识库</span>
        </el-menu-item>
        <el-menu-item index="/exam">
          <el-icon><Trophy /></el-icon>
          <span>考试冲刺</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ChatDotRound, User, Document, MapLocation, Collection, Trophy } from '@element-plus/icons-vue'

const route = useRoute()
const currentRoute = computed(() => route.path)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

.app-container {
  height: 100vh;
}

.app-aside {
  background-color: #304156;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-bottom: 1px solid #3d4f65;
}

.logo h2 {
  font-size: 18px;
  font-weight: 500;
}

.aside-menu {
  border-right: none;
}

.app-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
