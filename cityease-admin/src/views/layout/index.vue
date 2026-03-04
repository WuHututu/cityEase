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
              <el-avatar :size="32" :src="userInfo.avatar" />
              <span class="username">{{ userInfo.userName }}</span>
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
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" :key="route.path" />
            </transition>
          </router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Platform, DataLine, Tools, Money, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 引入您封装好的 request
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

// 响应式数据：存储用户信息，给定默认值防闪烁
const userInfo = ref({
  userName: '加载中...',
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
})

// 初始化时获取后端用户信息
const fetchUserInfo = async () => {
  try {
    // 调用后端 AdminUserController 里的 /admin/user/info 接口
    // request 拦截器已经处理了状态码 0 并剥离了外层 res.data
    const data: any = await request.get('/admin/user/info')
    if (data) {
      userInfo.value.userName = data.userName
      userInfo.value.avatar = data.avatar
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

onMounted(() => {
  fetchUserInfo()
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出当前系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        // 先调用后端接口注销 Sa-Token
        await request.post('/logout')
      } catch (error) {
        console.warn('后端注销失败，但前端将继续清理本地缓存', error)
      } finally {
        // 无论后端是否成功响应，前端都要清理凭证并跳转
        localStorage.removeItem('satoken')
        ElMessage.success('已安全退出')
        router.push('/login')
      }
    }).catch(() => {})
  }
}
</script>

<style scoped lang="scss">
/* 页面切换的过渡动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.layout-container {
  height: 100vh;
  width: 100vw;
  background-color: #0f172a;
}

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