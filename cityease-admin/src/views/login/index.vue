<template>
  <div class="login-container">
    <div class="glow-bg glow-1"></div>
    <div class="glow-bg glow-2"></div>

    <div class="login-box">
      <div class="login-header">
        <div class="logo-box">
          <el-icon class="logo-icon"><Platform /></el-icon>
        </div>
        <h2 class="title">城易治 CityEase</h2>
        <p class="subtitle">智慧物业数据指挥舱</p>
        <div class="feature-tags">
          <span>房屋管理</span>
          <span>报修协同</span>
          <span>费用追踪</span>
        </div>
      </div>

      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
      >
        <el-form-item prop="phone">
          <el-input
              v-model="loginForm.phone"
              placeholder="管理员账号"
              :prefix-icon="User"
              size="large"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="管理员密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup="onPasswordKeyup"
              @keyup.enter="handleLogin(loginFormRef)"
          />
        </el-form-item>

        <el-form-item class="extra">
          <el-checkbox v-model="rememberPhone">记住账号</el-checkbox>
          <span v-if="capsLockOn" class="caps-lock-tip">大写锁定已开启</span>
        </el-form-item>

        <el-form-item>
          <el-button
              :loading="loading"
              type="primary"
              class="login-btn"
              size="large"
              @click="handleLogin(loginFormRef)"
          >
            {{ loading ? '登录中...' : '进入系统' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Platform } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberPhone = ref(true)
const capsLockOn = ref(false)

const loginForm = reactive({
  phone: '',
  password: ''
})

const onPasswordKeyup = (event: KeyboardEvent) => {
  capsLockOn.value = event.getModifierState('CapsLock')
}

const rules = reactive<FormRules>({
  phone: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入11位手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
})

const handleLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return

  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用后端登录接口，注意替换为你实际的后台登录接口路径
        const res: any = await request.post('/login', {
          phone: loginForm.phone,
          password: loginForm.password
        })

        // 假设后端返回的数据中包含 token 字段
        localStorage.setItem('satoken', res)
        if (rememberPhone.value) {
          localStorage.setItem('cityease_admin_phone', loginForm.phone)
        } else {
          localStorage.removeItem('cityease_admin_phone')
        }

        ElMessage.success('登录成功，欢迎回来')
        // 跳转到大屏首页
        router.push('/dashboard')

      } catch (error) {
        // 错误已经在 request.ts 的响应拦截器中统一处理过了，这里无需额外打印
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  const cachedPhone = localStorage.getItem('cityease_admin_phone')
  if (cachedPhone) {
    loginForm.phone = cachedPhone
  }
})
</script>

<style scoped lang="scss">
.login-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  background-color: #0f172a;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(148, 163, 184, 0.06) 1px, transparent 1px),
  linear-gradient(90deg, rgba(148, 163, 184, 0.06) 1px, transparent 1px);
  background-size: 32px 32px;
  pointer-events: none;
}

/* 营造科技感的背景光晕 */
.glow-bg {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 0;
}

.glow-1 {
  width: 400px;
  height: 400px;
  background: rgba(24, 144, 255, 0.3);
  top: -100px;
  left: -100px;
}

.glow-2 {
  width: 500px;
  height: 500px;
  background: rgba(0, 240, 255, 0.2);
  bottom: -150px;
  right: -100px;
}

/* 玻璃拟态登录面板 */
.login-box {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: 40px 50px;
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-box {
  width: 60px;
  height: 60px;
  margin: 0 auto 15px;
  background: linear-gradient(135deg, #1890ff, #00f0ff);
  border-radius: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 10px 20px rgba(24, 144, 255, 0.3);
}

.logo-icon {
  font-size: 32px;
  color: #ffffff;
}

.title {
  margin: 0;
  font-size: 28px;
  color: #ffffff;
  font-weight: 600;
  letter-spacing: 1px;
}

.subtitle {
  margin: 10px 0 0;
  font-size: 14px;
  color: #94a3b8;
  letter-spacing: 2px;
}

.feature-tags {
  margin-top: 14px;
  display: flex;
  justify-content: center;
  gap: 8px;

  span {
    font-size: 12px;
    color: #cbd5e1;
    border: 1px solid rgba(148, 163, 184, 0.35);
    padding: 2px 8px;
    border-radius: 999px;
  }
}

.extra {
  margin-top: -4px;
  margin-bottom: 8px;
}

.caps-lock-tip {
  margin-left: auto;
  font-size: 12px;
  color: #f59e0b;
}

.login-form {
  margin-top: 30px;
}

/* 深度覆盖 Element Plus 的输入框默认样式以适配暗黑模式 */
:deep(.el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1890ff inset !important;
}

:deep(.el-input__inner) {
  color: #e2e8f0 !important;
}

:deep(.el-input__inner::placeholder) {
  color: #64748b !important;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  background: linear-gradient(90deg, #1890ff, #00b4db);
  border: none;
  font-weight: 600;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(24, 144, 255, 0.4);
}
</style>
