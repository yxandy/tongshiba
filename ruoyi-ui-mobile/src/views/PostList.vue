<template>
  <div class="post-list-page">
    <!-- 非企业微信环境提示 -->
    <div v-if="!isWxWork && !isDev" class="access-denied">
      <van-icon name="warning-o" size="60" />
      <p>{{ accessDeniedMessage }}</p>
    </div>

    <!-- 正常内容 -->
    <template v-else>
      <!-- 顶部导航 (移除标题，添加右侧菜单) -->
      <van-nav-bar fixed placeholder>
        <template #right>
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

      <!-- 下拉刷新 + 帖子列表 -->
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="loadMore"
        >
          <!-- 帖子列表项 -->
          <div 
            v-for="post in postList" 
            :key="post.postId" 
            class="post-item"
            @click="goToDetail(post.postId)"
          >
            <!-- 标题 -->
            <div class="post-title">{{ post.title }}</div>
            
            <!-- 内容摘要 (支持表情渲染) -->
            <div class="post-content" v-html="truncateContent(post.content)"></div>
            
            <!-- 图片预览 (缩略图) -->
            <div v-if="post.images && getImages(post.images).length > 0" class="post-images">
              <van-image
                v-for="(img, idx) in getImages(post.images).slice(0, 3)"
                :key="idx"
                width="80"
                height="80"
                fit="cover"
                :src="img"
                class="post-image"
              />
              <span v-if="getImages(post.images).length > 3" class="more-images">
                +{{ getImages(post.images).length - 3 }}
              </span>
            </div>

            <!-- 底部信息 -->
            <div class="post-footer">
              <span class="author-name">{{ post.user?.nickname || '匿名用户' }}</span>
              <div class="post-stats">
                <span><van-icon name="eye-o" /> {{ post.viewCount || 0 }}</span>
                <span><van-icon name="comment-o" /> {{ post.commentCount || 0 }}</span>
              </div>
            </div>
          </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList, syncUser } from '@/api/forum'
import { isWxWorkEnv, getAccessDeniedMessage, getMockUser } from '@/utils/wxwork'
import { renderEmojis } from '@/config/emojis'

const router = useRouter()

// 状态
const isWxWork = ref(true)
const isDev = ref(import.meta.env.DEV)
const accessDeniedMessage = ref(getAccessDeniedMessage())
const postList = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)

// 菜单
const showMenu = ref(false)
const menuActions = [
  { text: '关注的帖子', value: 'followed' },
  { text: '发过的帖子', value: 'myPosts' }
]

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
  
  // 开发环境或企业微信环境，同步用户并加载数据
  if (isDev.value || isWxWork.value) {
    await initUser()
    loadMore()
  }
})

// 初始化用户
async function initUser() {
  // 开发环境使用模拟数据
  if (isDev.value && !isWxWork.value) {
    const mockUser = getMockUser()
    try {
      const res = await syncUser(mockUser)
      // res.data 才是真正的用户信息 (包含 userId)
      const userData = res.data || mockUser
      localStorage.setItem('forumUser', JSON.stringify(userData))
    } catch (e) {
      console.error('同步用户失败', e)
    }
  }
}

// 加载更多
async function loadMore() {
  if (finished.value) return
  
  loading.value = true
  try {
    const res = await getPostList({ pageNum: pageNum.value, pageSize: pageSize.value })
    const rows = res.rows || []
    
    if (pageNum.value === 1) {
      postList.value = rows
    } else {
      postList.value.push(...rows)
    }
    
    if (rows.length < pageSize.value) {
      finished.value = true
    } else {
      pageNum.value++
    }
  } catch (e) {
    console.error('加载帖子失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
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

// 截断内容 (并渲染表情)
function truncateContent(content) {
  if (!content) return ''
  const truncated = content.length > 50 ? content.substring(0, 50) + '...' : content
  return renderEmojis(truncated)
}

// 解析图片
function getImages(images) {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}
</script>

<style scoped>
.post-list-page {
  min-height: 100vh;
  background: var(--bg-color); /* 保持灰色背景 */
  padding-bottom: 60px; /* 为底部栏留出空间 */
}

.access-denied {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  color: var(--text-secondary);
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
}

@media (prefers-color-scheme: dark) {
  .post-title {
    color: #f5f5f5;
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

.post-images {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
  align-items: center;
}

.post-image {
  border-radius: 4px;
}

.more-images {
  font-size: 12px;
  color: var(--text-secondary);
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
  height: 50px;
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
