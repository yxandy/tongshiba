<template>
  <div v-if="isAllowed">
    <router-view v-slot="{ Component }">
      <keep-alive :include="['PostSearch', 'PostList']">
        <component :is="Component" />
      </keep-alive>
    </router-view>
  </div>
  <div v-else class="access-denied">
    <div class="denied-content">
      <van-icon name="info" color="#1989fa" size="64" />
      <p class="denied-text">仅企业内可查看</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { isWxWorkEnv } from '@/utils/wxwork'

const isAllowed = ref(true)

onMounted(() => {
  // 生产环境强制检查企业微信环境
  // 本地开发可以通过 ?debug=true 绕过
  const isDebug = new URLSearchParams(window.location.search).get('debug') === 'true'
  
  if (!isWxWorkEnv() && !isDebug) {
    isAllowed.value = false
    // 设置黑色背景
    document.body.style.backgroundColor = '#000000'
  }
})
</script>

<style>
:root {
  --primary-color: #1989fa;
  --bg-color: #f7f8fa;
  --text-primary: #323233;
  --text-secondary: #969799;
  --border-color: #ebedf0;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  :root {
    --primary-color: #4da3ff;
    --bg-color: #1a1a1a;
    --text-primary: #f5f5f5;
    --text-secondary: #a0a0a0;
    --border-color: #3a3a3a;
  }
  
  body {
    background-color: var(--bg-color);
    color: var(--text-primary);
  }
  
  /* Vant 导航栏深色适配 */
  .nav-bar-dark,
  .van-nav-bar {
    background: #191919 !important;
  }
  .nav-bar-dark .van-nav-bar__title,
  .van-nav-bar__title {
    color: #e5e5e5 !important;
  }
  .nav-bar-dark .van-nav-bar__text,
  .van-nav-bar__text {
    color: #e5e5e5 !important;
  }
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  /* 禁用双击缩放手势，但保留正常的触摸滚动 */
  touch-action: manipulation;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: var(--bg-color);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

html {
  /* 恢复默认，让每个页面自己控制滚动 */
}

#app {
  min-height: 100vh;
}

.access-denied {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #000000;
  color: #ffffff;
}

.denied-content {
  text-align: center;
  margin-top: -30%;
}

.denied-text {
  margin-top: 20px;
  font-size: 18px;
  color: #ffffff;
  letter-spacing: 1px;
}
</style>
