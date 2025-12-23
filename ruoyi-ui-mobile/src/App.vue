<template>
  <router-view />
</template>

<script setup>
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
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
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
</style>
