<template>
  <div class="my-posts-page">
    <!-- 顶部导航 -->
    <van-nav-bar 
      title="发过的帖子" 
      left-text="返回"
      left-arrow 
      fixed 
      placeholder
      @click-left="goBack"
    />

    <!-- 空状态 -->
    <div v-if="!loading && posts.length === 0" class="empty-state">
      <div class="empty-icon">
        <van-icon name="notes-o" size="64" color="#a0d8ef" />
      </div>
      <p class="empty-text">你还没有发过帖子</p>
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
          <span class="time">{{ formatTime(post.createTime) }}</span>
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
import { getMyPosts } from '@/api/forum'

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
    if (!user.userId) {
      finished.value = true
      return
    }

    const res = await getMyPosts(user.userId, { pageNum: pageNum.value, pageSize: 20 })
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
    console.error('加载我的帖子失败', e)
    finished.value = true
  } finally {
    loading.value = false
  }
}

function truncateContent(content) {
  if (!content) return ''
  const plain = content.replace(/\[[^\]]+\]/g, '')
  return plain.length > 60 ? plain.slice(0, 60) + '...' : plain
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  return `${month}-${day}`
}

function goToDetail(postId) {
  router.push(`/post/${postId}`)
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.my-posts-page {
  min-height: 100vh;
  background: var(--bg-color, #f5f5f5);
  padding-bottom: 20px;
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
  padding: 16px;
  margin: 10px 0;
  position: relative;
}

.post-card:first-child {
  margin-top: 0;
}

.post-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8px;
}

.post-preview {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #999;
}

.time {
  color: #999;
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

@media (prefers-color-scheme: dark) {
  .post-card {
    background: #2c2c2c;
  }
  .post-title {
    color: #e5e5e5;
  }
  .post-preview {
    color: #999;
  }
}
</style>
