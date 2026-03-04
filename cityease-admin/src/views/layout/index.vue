<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside-menu">
      <div class="logo-box">
        <el-icon class="logo-icon"><Platform /></el-icon>
        <span v-show="!isCollapse" class="logo-text">CityEase</span>
      </div>

      <el-menu
          active-text-color="#00f0ff"
          background-color="#0f172a"
          class="el-menu-vertical"
          :default-active="route.path"
          text-color="#94a3b8"
          :collapse="isCollapse"
          router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>数据大屏</template>
        </el-menu-item>
        <el-menu-item index="/repair">
          <el-icon><Tools /></el-icon>
          <template #title>报修工单</template>
        </el-menu-item>
        <el-menu-item index="/fee">
          <el-icon><Money /></el-icon>
          <template #title>财务账单</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="fold-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse"/>
            <Expand v-else/>
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>工作台</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">管理员</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <div class="content-box">
          <h2 style="color: #cbd5e1; text-align: center; margin-top: 100px;">这里是内容区，马上接入Dashboard数据</h2>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Platform, DataLine, Tools, Money, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出当前系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      localStorage.removeItem('satoken')
      ElMessage.success('已安全退出')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  width: 100vw;
  background-color: #0f172a; /* 整体深色背景 */
}

/* 左侧边栏 */
.aside-menu {
  background-color: #0f172a;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  transition: width 0.3s;
  overflow: hidden;

  .logo-box {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);

    .logo-icon {
      font-size: 24px;
      color: #1890ff;
    }

    .logo-text {
      margin-left: 10px;
      font-size: 20px;
      font-weight: bold;
      white-space: nowrap;
    }
  }

  .el-menu-vertical {
    border-right: none;
  }
}

/* 顶部导航 */
.header {
  height: 60px;
  background-color: rgba(30, 41, 59, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .header-left {
    display: flex;
    align-items: center;

    .fold-btn {
      font-size: 22px;
      color: #94a3b8;
      cursor: pointer;
      margin-right: 20px;
      &:hover { color: #fff; }
    }

    /* 深度修改面包屑文字颜色以适配暗黑主题 */
    :deep(.el-breadcrumb__inner) { color: #94a3b8 !important; }
    :deep(.el-breadcrumb__inner.is-link:hover) { color: #1890ff !important; }
  }

  .header-right {
    .user-dropdown {
      display: flex;
      align-items: center;
      cursor: pointer;
      color: #e2e8f0;

      .username { margin: 0 8px; }
    }
  }
}

/* 主内容区 */
.main-content {
  padding: 20px;
  background-color: #0f172a;

  .content-box {
    height: 100%;
    background-color: rgba(30, 41, 59, 0.5);
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.05);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    padding: 20px;
    overflow: auto;
  }
}
</style>