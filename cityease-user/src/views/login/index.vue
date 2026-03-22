<template>
  <div class="login-page">
    <div class="login-header">
      <h1>CityEase</h1>
      <p>智慧社区服务平台</p>
    </div>
    
    <div class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          label="手机号"
          placeholder="请输入手机号"
          type="tel"
          :rules="[{ required: true, message: '请填写手机号' }]"
        />
        <van-field
          v-model="form.password"
          label="密码"
          placeholder="请输入密码"
          type="password"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      
      <div class="login-btn">
        <van-button type="primary" block round @click="handleLogin" :loading="loading">
          登录
        </van-button>
      </div>
      
      <div class="login-tips">
        <p>测试账号: 13800138000 / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { login } from '@/api/auth'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.phone || !form.password) {
    showToast('请填写完整信息')
    return
  }
  
  loading.value = true
  showLoadingToast({
    message: '登录中...',
    forbidClick: true
  })
  
  try {
    const token = await login(form)
    localStorage.setItem('satoken', token)
    localStorage.setItem('userInfo', JSON.stringify({ phone: form.phone }))
    
    closeToast()
    showToast('登录成功')
    router.push('/home')
  } catch (error: any) {
    showToast(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-header {
  text-align: center;
  color: #fff;
  margin-bottom: 40px;
  
  h1 {
    font-size: 36px;
    font-weight: bold;
    margin-bottom: 10px;
  }
  
  p {
    font-size: 16px;
    opacity: 0.9;
  }
}

.login-form {
  width: 100%;
  max-width: 350px;
}

.login-btn {
  margin-top: 30px;
  padding: 0 16px;
}

.login-tips {
  text-align: center;
  margin-top: 20px;
  color: #fff;
  opacity: 0.8;
  font-size: 12px;
}
</style>
