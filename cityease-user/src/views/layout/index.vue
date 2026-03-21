<template>
  <div class="layout">
    <div class="content">
      <router-view />
    </div>
    
    <van-tabbar v-model="active" @change="onTabChange">
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="shop-o" to="/mall">商城</van-tabbar-item>
      <van-tabbar-item icon="records" to="/repair">报修</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/user">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const active = ref(0)

const tabRoutes = ['/home', '/mall', '/repair', '/user']

// 根据当前路由设置激活的 tab
watch(() => route.path, (path) => {
  const index = tabRoutes.findIndex(route => path.startsWith(route))
  if (index !== -1) {
    active.value = index
  }
}, { immediate: true })

const onTabChange = (index: number) => {
  router.push(tabRoutes[index])
}
</script>

<style scoped lang="scss">
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 50px;
}
</style>
