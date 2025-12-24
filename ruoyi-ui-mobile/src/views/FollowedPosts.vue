<template>
  <div class="followed-posts-page">
    <!-- 顶部导航 -->
    <van-nav-bar 
      title="关注的帖子" 
      fixed 
      placeholder
      class="nav-bar-dark"
    />

    <!-- 空状态 -->
    <div v-if="!loading && posts.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 100 100" width="80" height="80">
          <rect x="20" y="10" width="60" height="80" fill="none" stroke="#a0d8ef" stroke-width="3" rx="4"/>
          <polygon points="50,35 55,45 65,47 58,55 60,65 50,60 40,65 42,55 35,47 45,45" fill="none" stroke="#a0d8ef" stroke-width="2"/>
        </svg>
      </div>
      <p class="empty-text">你还没有关注过帖子</p>
    </div>

    <!-- 帖子列表 -->
    <van-list
      v-else
      v-model:loading="loading"
      :finished="finished"
      finished-text=""
      @load="loadMore"
    >
      <div 
        v-for="post in posts" 
        :key="post.postId" 
        class="post-card"
        @click="goToDetail(post.postId)"
      >
        <div class="post-title">{{ post.title }}</div>
        <div class="post-preview">{{ truncateContent(post.content) }}</div>
        <div class="post-footer">
          <span class="author">{{ post.user?.nickname || '匿名用户' }}</span>
          <div class="stats">
            <span class="stat"><van-icon name="eye-o" /> {{ post.viewCount || 0 }}</span>
            <span class="stat"><van-icon name="chat-o" /> {{ post.commentCount || 0 }}</span>
          </div>
        </div>
        <!-- 锁定标识 -->
        <div v-if="post.isLocked === '1'" class="locked-badge">
          <van-icon name="lock" /> 已锁定
        </div>
      </div>
    </van-list>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFollowedPosts } from '@/api/forum'

const router = useRouter()
const posts = ref([])
const loading = ref(false)
const finished = ref(false)
const pageNum = ref(1)

onMounted(() => {
  loadMore()
})

async function loadMore() {
  if (finished.value) return
  loading.value = true

  try {
    const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
    if (!user.wxUserid) {
      finished.value = true
      return
    }

    const res = await getFollowedPosts(user.wxUserid, { pageNum: pageNum.value, pageSize: 20 })
    const rows = res.rows || []

    if (pageNum.value === 1) {
      posts.value = rows
    } else {
      posts.value.push(...rows)
    }

    if (rows.length < 20) {
      finished.value = true
    } else {
      pageNum.value++
    }
  } catch (e) {
    console.error('加载关注列表失败', e)
    finished.value = true
  } finally {
    loading.value = false
  }
}

function truncateContent(content) {
  if (!content) return ''
  // 移除表情标签
  const plain = content.replace(/\[[^\]]+\]/g, '')
  return plain.length > 60 ? plain.slice(0, 60) + '...' : plain
}

function goToDetail(postId) {
  router.push(`/post/${postId}`)
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.followed-posts-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 20px;
}

@media (prefers-color-scheme: dark) {
  .followed-posts-page {
    background: #000;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #999;
}

.empty-icon {
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
  color: #6abac2;
}

.post-card {
  background: #fff;
  padding: 16px 16px 12px 16px;
  margin-bottom: 10px;
  position: relative;
}

@media (prefers-color-scheme: dark) {
  .post-card {
    background: #191919;
  }
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #000;
  line-height: 1.4;
  margin-bottom: 8px;
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

.post-preview {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #999;
}

.author {
  color: #999;
}

@media (prefers-color-scheme: dark) {
  .author {
    color: #7d90a9;
  }
}

.stats {
  display: flex;
  gap: 16px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

.locked-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 12px;
  color: #ee0a24;
  display: flex;
  align-items: center;
  gap: 2px;
}
</style>
