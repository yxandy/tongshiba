import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  base: '/tongshiba/', // 基础路径
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    host: '0.0.0.0',  // 允许局域网访问
    allowedHosts: ['hnfz.sdecl.com.cn'],  // 允许的域名
    proxy: {
      '/tongshiba-api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 将 /tongshiba-api 替换为 /tongshiba（后端的 context-path）
        rewrite: (path) => path.replace(/^\/tongshiba-api/, '/tongshiba')
      }
    }
  }
})
