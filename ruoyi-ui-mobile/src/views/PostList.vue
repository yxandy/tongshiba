<template>
  <div class="post-list-page">
    
    <!-- 非企业微信环境提示 -->
    <div v-if="!isWxWork && !isDev" class="access-denied">
      <van-icon name="warning-o" size="60" />
      <p>{{ accessDeniedMessage }}</p>
    </div>

    <!-- 正常内容 -->
    <template v-else>
      <!-- 顶部导航 -->
      <van-nav-bar title="帖子列表" fixed placeholder class="nav-bar-dark">
        <template #right>
          <van-icon name="search" size="22" style="margin-right: 16px" @click="goToSearch" />
          <van-popover 
            v-model:show="showMenu" 
            :actions="menuActions" 
            @select="onMenuSelect"
            placement="bottom-end"
          >
            <template #reference>
              <van-icon name="ellipsis" size="22" />
            </template>
          </van-popover>
        </template>
      </van-nav-bar>

      <!-- 分类筛选 -->
      <div class="category-filter">
        <div class="filter-scroll">
          <span 
            class="filter-tag" 
            :class="{ active: selectedCategoryId === null }"
            @click="selectedCategoryId = null"
          >全部</span>
          <span 
            v-for="cat in categories" 
            :key="cat.categoryId"
            class="filter-tag"
            :class="{ active: selectedCategoryId === cat.categoryId }"
            @click="selectedCategoryId = cat.categoryId"
          >{{ cat.name }}</span>
        </div>
      </div>

      <!-- 骨架屏（首次加载时显示） -->
      <div v-if="isFirstLoad" class="skeleton-container">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <van-skeleton title :row="2" />
          <div class="skeleton-footer">
            <van-skeleton-paragraph row-width="30%" />
          </div>
        </div>
      </div>

      <!-- 下拉刷新 + 帖子列表 -->
      <van-pull-refresh v-else v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="loadMore"
        >
          <!-- 帖子列表项 -->
          <PostItem
            v-for="post in postList"
            :key="post.postId"
            :post="post"
            @click="goToDetail(post.postId)"
          />
        </van-list>
      </van-pull-refresh>

      <!-- 发帖按钮 -->
      <div class="create-btn" @click="goToCreate">
        <span class="create-text">
          <van-icon name="edit" style="margin-right: 6px;" />
          发表帖子
        </span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList, syncUser, getCategoryList } from '@/api/forum'
import { isWxWorkEnv, getAccessDeniedMessage, getMockUser, loginWithWxWork } from '@/utils/wxwork'
import PostItem from '@/components/PostItem.vue'

const router = useRouter()

// 状态
const isWxWork = ref(true)
const isDev = ref(import.meta.env.DEV)
const accessDeniedMessage = ref(getAccessDeniedMessage())
const postList = ref([])
const loading = ref(false)
const isFirstLoad = ref(true)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const debugInfo = ref('')  // 调试信息

// 菜单
const showMenu = ref(false)
const menuActions = [
  { text: '关注的帖子', value: 'followed' },
  { text: '发过的帖子', value: 'myPosts' }
]

// 分类数据
const categories = ref([])
const selectedCategoryId = ref(null)

// 监听分类变化，自动刷新列表
watch(selectedCategoryId, () => {
  pageNum.value = 1
  finished.value = false
  loading.value = false
  postList.value = []
  loadMore()
})

function onMenuSelect(action) {
  showMenu.value = false
  if (action.value === 'followed') {
    router.push('/followed')
  } else if (action.value === 'myPosts') {
    router.push('/my-posts')
  }
}

// 检查环境
onMounted(async () => {
  isWxWork.value = isWxWorkEnv()
  
  // 加载分类列表
  try {
    const res = await getCategoryList()
    if (res.data) {
      categories.value = res.data
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
  
  // 开发环境或企业微信环境，同步用户信息
  if (isDev.value || isWxWork.value) {
    await initUser()
    // 手动加载数据（因为骨架屏显示时 van-list 未渲染，无法自动触发 @load）
    loadMore()
  }
})

// 从其他页面返回时刷新列表（keep-alive 激活）
onActivated(() => {
  // 重置为第一页并刷新
  postList.value = []
  pageNum.value = 1
  finished.value = false
  loadMore()
})

// 初始化用户
async function initUser() {
  let debug = []
  debug.push('URL: ' + window.location.href)
  debug.push('userid参数: ' + new URLSearchParams(window.location.search).get('userid'))
  
  // 1. 先尝试企业微信登录（检测 URL 中的 userid）
  try {
    const wxUser = await loginWithWxWork()
    debug.push('loginWithWxWork返回: ' + JSON.stringify(wxUser))
    
    if (wxUser) {
      // 同步用户到后端数据库
      try {
        const res = await syncUser(wxUser)
        const userData = res.data || wxUser
        localStorage.setItem('forumUser', JSON.stringify(userData))
        debug.push('syncUser成功: ' + JSON.stringify(userData))
      } catch (e) {
        debug.push('syncUser失败: ' + e.message)
      }
      debugInfo.value = debug.join('\n')
      return
    }
  } catch (e) {
    debug.push('loginWithWxWork异常: ' + e.message)
  }
  
  // 2. 开发环境使用模拟数据
  if (isDev.value && !isWxWork.value) {
    debug.push('开发环境，使用模拟用户')
    const mockUser = getMockUser()
    try {
      const res = await syncUser(mockUser)
      const userData = res.data || mockUser
      localStorage.setItem('forumUser', JSON.stringify(userData))
    } catch (e) {
      debug.push('同步用户失败: ' + e.message)
    }
  }
  
  debugInfo.value = debug.join('\n')
}

// 加载更多
async function loadMore() {
  if (finished.value) return
  
  loading.value = true
  try {
    const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }
    // 传入 wxUserid 用于限流过滤
    if (user.wxUserid) {
      params.wxUserid = user.wxUserid
    }
    const res = await getPostList(params)
    const rows = res.rows || []
    const total = res.total || 0
    
    if (pageNum.value === 1) {
      postList.value = rows
    } else {
      postList.value.push(...rows)
    }
    
    // 使用 total 字段判断是否已加载完毕
    if (postList.value.length >= total || rows.length === 0) {
      finished.value = true
    } else {
      pageNum.value++
    }
  } catch (e) {
    console.error('加载帖子失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
    isFirstLoad.value = false
  }
}

// 下拉刷新
function onRefresh() {
  pageNum.value = 1
  finished.value = false
  loadMore()
}

// 跳转详情
function goToDetail(postId) {
  router.push(`/post/${postId}`)
}

// 跳转发帖
function goToCreate() {
  router.push('/post/create')
}

// 跳转搜索
function goToSearch() {
  router.push('/post/search')
}
</script>

<style scoped>
/* 骨架屏样式 */
.skeleton-container {
  padding: 0 16px;
}

.skeleton-card {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
}

.skeleton-footer {
  margin-top: 12px;
}

@media (prefers-color-scheme: dark) {
  .skeleton-card {
    background: #1a1a1a;
  }
}

.debug-panel {
  position: fixed;
  top: 50px;
  left: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.85);
  color: #0f0;
  padding: 12px;
  border-radius: 8px;
  z-index: 9999;
  font-size: 12px;
  max-height: 300px;
  overflow: auto;
}
.debug-panel pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}
.debug-panel small {
  display: block;
  margin-top: 8px;
  color: #999;
  text-align: center;
}

/* 导航栏深色主题 */
@media (prefers-color-scheme: dark) {
  .nav-bar-dark :deep(.van-nav-bar) {
    background: #191919;
  }
  .nav-bar-dark :deep(.van-nav-bar__title) {
    color: #f5f5f5;
  }
  .nav-bar-dark :deep(.van-icon) {
    color: #f5f5f5;
  }
}

.post-list-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 46px; /* 分类筛选栏高度 */
  padding-bottom: 85px; /* 配合更高的发帖按钮 */
}

@media (prefers-color-scheme: dark) {
  .post-list-page {
    background: #000;
  }
}

.access-denied {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  color: var(--text-secondary);
}

/* 分类筛选栏 */
.category-filter {
  position: fixed;
  top: 46px; /* 导航栏高度 */
  left: 0;
  right: 0;
  z-index: 99;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.filter-scroll {
  display: flex;
  overflow-x: auto;
  padding: 10px 16px;
  gap: 12px;
  -webkit-overflow-scrolling: touch;
}

.filter-scroll::-webkit-scrollbar {
  display: none;
}

.filter-tag {
  flex-shrink: 0;
  padding: 5px 14px;
  font-size: 14px;
  color: #666;
  background: #f5f5f5;
  border-radius: 14px;
  white-space: nowrap;
}

.filter-tag.active {
  color: #fff;
  background: #1989fa;
}

@media (prefers-color-scheme: dark) {
  .category-filter {
    background: #191919;
    border-bottom-color: #333;
  }
  .filter-tag {
    color: #aaa;
    background: #2c2c2c;
  }
  .filter-tag.active {
    color: #fff;
    background: #1989fa;
  }
}

.post-item {
  background: #fff;
  padding: 16px 16px 12px 16px;
  margin-bottom: 10px; /* 灰色间隔 */
  border-bottom: none; /* 移除原来的线条，用间隔代替 */
}

@media (prefers-color-scheme: dark) {
  .post-item {
    background: #191919;
    margin-bottom: 10px;
    border-bottom: none;
  }
}

.post-title {
  font-size: 18px; /* 标题字号加大 */
  font-weight: 600;
  margin-bottom: 8px;
  color: #000;
  line-height: 1.4;
  /* 限制最多 2 行，超出省略 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

@media (prefers-color-scheme: dark) {
  .post-title {
    color: #f5f5f5;
  }
}

/* 置顶标签样式 */
.pin-tag {
  color: #ff6600;
  font-weight: bold;
  margin-right: 4px;
}

@media (prefers-color-scheme: dark) {
  .pin-tag {
    color: #ff9500;
  }
}

.post-content {
  font-size: 15px; /* 内容字号 */
  color: #666; /* 浅灰色 */
  line-height: 1.6;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 纯图片帖子的图标 */
.post-image-icon {
  color: #999;
  margin-bottom: 12px;
}

@media (prefers-color-scheme: dark) {
  .post-image-icon {
    color: #666;
  }
}

.post-video-icon {
  color: #999;
  margin-bottom: 12px;
}

@media (prefers-color-scheme: dark) {
  .post-video-icon {
    color: #666;
  }
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px; /* 底部信息字号 */
  color: #999;
}

.author-name {
  color: #999; /* 截图里作者名字似乎也是灰色，不是蓝色链接色 */
}

@media (prefers-color-scheme: dark) {
  .author-name {
    color: #7d90a9;
  }
}

.post-stats {
  display: flex;
  gap: 16px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 底部发帖栏 */
.create-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 75px; /* 增加高度，更容易点击 */
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid #eee; /* 浅分割线 */
  z-index: 100;
  cursor: pointer;
  color: #333;
}

@media (prefers-color-scheme: dark) {
  .create-btn {
    background: #191919;
    border-top: 1px solid #333;
    color: #f5f5f5;
  }
}

.create-text {
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.create-text::before {
  content: ''; /* 使用 Icon 组件代替背景图更灵活，这里先移除伪元素，在 template 中加 icon */
  display: none; 
} 
</style>
