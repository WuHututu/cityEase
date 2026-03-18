import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 配置 @ 指向 src 目录，方便引入文件
      '@': path.resolve(__dirname, './src')
    }
  },
  optimizeDeps: {
    include: ['element-plus', 'axios'],
    exclude: ['@wangeditor/editor-for-vue']
  },
  server: {
    port: 5173,
    proxy: {
      // 拦截所有带 /api 前缀的请求，转发给 Spring Boot
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 将 /api 替换为空，因为后端接口通常不带 /api
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})