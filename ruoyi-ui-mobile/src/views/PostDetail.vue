<template>
  <div class="post-detail-page">
    <!-- 非企业微信环境提示 -->
    <div v-if="!isWxWork && !isDev" class="access-denied">
      <van-icon name="warning-o" size="60" />
      <p>{{ accessDeniedMessage }}</p>
    </div>

    <!-- 正常内容 -->
    <template v-else>
      <!-- 顶部导航 -->
      <van-nav-bar 
        title="帖子详情" 
        left-arrow 
        fixed 
        placeholder
        @click-left="goBack"
      >
        <template #right>
          <van-icon name="share-o" size="20" @click="handleShare" />
        </template>
      </van-nav-bar>

      <!-- 帖子内容 -->
      <div v-if="post" class="post-content-wrap">
        <!-- 标题 -->
        <h1 class="post-title">{{ post.title }}</h1>
        
        <!-- 正文 (支持表情渲染) -->
        <div class="post-body" v-html="renderEmojis(post.content)"></div>

        <!-- 图片展示 -->
        <div v-if="images.length > 0" class="post-images">
          <van-image
            v-for="(img, idx) in images"
            :key="idx"
            width="100%"
            fit="cover"
            :src="img"
            class="post-image"
            @click="previewImage(idx)"
          />
        </div>

        <!-- 底部元信息 (作者 · 时间 · 删除) -->
        <div class="post-meta-footer">
          <span class="meta-author">{{ post.user?.nickname || '匿名用户' }}</span>
          <span class="meta-separator">·</span>
          <span class="meta-time">{{ formatTime(post.createTime) }}</span>
          <span v-if="post.isLocked === '1'" class="locked">
            <span class="meta-separator">·</span>
            <van-icon name="lock" /> 已锁定
          </span>
          <!-- 删除按钮，作者或管理员可删除 -->
          <span class="delete-btn" v-if="canDelete" @click="confirmDelete">删除</span>
        </div>

        <div class="post-stats-row">
          <div class="stats-left">
             <div class="stat-item">
               <van-icon name="eye-o" />
               <span>{{ post.viewCount || 0 }}</span>
             </div>
             <div class="stat-item">
               <van-icon name="chat-o" />
               <span>{{ post.commentCount || 0 }}</span>
             </div>
          </div>
          <div class="stats-right">
             <div 
               class="stat-item star-btn" 
               :class="{ 'followed': isFollowed }"
               @click="toggleFollow"
             >
               <van-icon :name="isFollowed ? 'star' : 'star-o'" :color="isFollowed ? '#ffc107' : ''" />
               <span>{{ isFollowed ? '已关注' : '关注' }}</span>
             </div>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <!-- 移除之前的标题 Comment (0) -->
        
        <van-list
          v-model:loading="commentLoading"
          :finished="commentFinished"
          finished-text=""
          @load="loadComments"
        >
          <div 
            v-for="comment in comments" 
            :key="comment.commentId" 
            class="comment-item"
          >
            <van-image
              width="36"
              height="36"
              radius="4"
              :src="comment.user?.avatar || defaultAvatar"
            />
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-nickname">{{ comment.user?.nickname || '匿名用户' }}</span>
                <span v-if="post && comment.userId === post.userId" class="landlord-tag">· 楼主</span>
              </div>
              <div class="comment-text" v-html="renderEmojis(comment.content)"></div>
              <div class="comment-meta">
                <span class="floor-num">{{ comment.floorNum }}楼</span>
                <span class="meta-dot">·</span>
                <span class="time">{{ formatTimeDayTime(comment.createTime) }}</span>
              </div>
            </div>
          </div>
        </van-list>

        <!-- 空状态：增加顶部留白，居中显示 -->
        <div v-if="comments.length === 0 && !commentLoading" class="no-comments">
          沙发还在，快来抢啊...
        </div>
      </div>

      <!-- 底部评论操作区 (常驻+表情面板) -->
      <div v-if="post?.isLocked !== '1'" class="comment-action-area">
        <!-- 输入栏 -->
        <div class="comment-input-bar">
          <div 
            class="input-wrap"
            :class="{ 'expanded': isInputExpanded }"
          >
            <textarea
              v-model="commentText"
              :rows="isInputExpanded ? 4 : 1"
              placeholder="发表评论..."
              class="native-textarea"
              ref="commentInputRef"
              @focus="isFocused = true; showEmojiPicker = false"
              @blur="handleBlur"
            ></textarea>
          </div>
          <van-icon 
            :name="showEmojiPicker ? 'smile' : 'smile-o'" 
            size="28" 
            :color="showEmojiPicker ? '#1989fa' : '#333'"
            @click="toggleEmojiPicker" 
          />
        </div>

        <!-- 表情选择器 (嵌入在底部区域中，而非弹窗) -->
        <div v-show="showEmojiPicker" class="emoji-picker-panel">
          <div class="emoji-picker">
            <!-- 最近使用 -->
            <div v-if="recentEmojis.length > 0" class="emoji-section">
              <div class="section-title">最近使用</div>
              <div class="emoji-grid">
                <div 
                  v-for="emoji in recentEmojis" 
                  :key="'recent-' + emoji.id" 
                  class="emoji-item"
                  @click="insertEmoji(emoji)"
                >
                  <img :src="emojiBasePath + emoji.file" :alt="emoji.name" />
                </div>
              </div>
            </div>
            <!-- 全部表情 -->
            <div class="emoji-section">
              <div class="section-title">全部</div>
              <div class="emoji-grid">
                <div 
                  v-for="emoji in emojiList" 
                  :key="emoji.id" 
                  class="emoji-item"
                  @click="insertEmoji(emoji)"
                >
                  <img :src="emojiBasePath + emoji.file" :alt="emoji.name" />
                </div>
              </div>
            </div>
          </div>
          
          <!-- 悬浮操作按钮 (删除/发送) -->
          <div class="emoji-actions">
            <div class="action-btn delete-btn" @click="deleteEmojiOrChar">
               <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" style="display: block;">
                 <path d="M22 3H7c-.69 0-1.23.35-1.59.88L0 12l5.41 8.11c.36.53.9.89 1.59.89h15c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-3 12.59L17.59 17 14 13.41 10.41 17 9 15.59 12.59 12 9 8.41 10.41 7 14 10.59 17.59 7 19 8.41 15.41 12 19 15.59z"/>
               </svg>
            </div>
            <div 
              class="action-btn send-btn" 
              :class="{ disabled: !commentText.trim() }"
              @click="submitComment"
            >
              发送
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showImagePreview, showConfirmDialog } from 'vant'
import { getPostDetail, getCommentList, createComment, syncUser, checkFollow, followPost, unfollowPost, deletePost } from '@/api/forum'
import { isWxWorkEnv, getAccessDeniedMessage, shareToUsers, getMockUser } from '@/utils/wxwork'
import { emojiList, emojiBasePath, renderEmojis, getRecentEmojis, addRecentEmoji } from '@/config/emojis'

const route = useRoute()
const router = useRouter()

// 状态
const isWxWork = ref(true)
const isDev = ref(import.meta.env.DEV)
const accessDeniedMessage = ref(getAccessDeniedMessage())
const post = ref(null)
const comments = ref([])
const commentLoading = ref(false)
const commentFinished = ref(false)
const isFollowed = ref(false)
const commentPageNum = ref(1)

// const showCommentPopup = ref(false) // 移除旧变量
const showEmojiPicker = ref(false)
const commentText = ref('')
const commentInputRef = ref(null) // 输入框引用
const isFocused = ref(false)
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

// 输入框是否展开
const isInputExpanded = computed(() => {
  return isFocused.value || showEmojiPicker.value || commentText.value.length > 0
})

function handleBlur() {
  // 延迟失焦，防止点击发送按钮时立即收起
  setTimeout(() => {
    isFocused.value = false
  }, 100)
}

// 最近使用的表情
const recentEmojis = ref(getRecentEmojis())

// 计算是否是作者
const isAuthor = computed(() => {
  if (!post.value) return false
  const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
  return post.value.userId === user.userId
})

// 计算是否是管理员
const isAdmin = computed(() => {
  const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
  return user.isAdmin === '1'
})

// 计算是否可以删除（作者或管理员）
const canDelete = computed(() => {
  return isAuthor.value || isAdmin.value
})

// 计算图片列表
const images = computed(() => {
  if (!post.value?.images) return []
  try {
    return JSON.parse(post.value.images)
  } catch {
    return []
  }
})

onMounted(async () => {
  isWxWork.value = isWxWorkEnv()
  
  if (isDev.value || isWxWork.value) {
    await initUser()
    await loadPost()
  }
})

// 初始化用户
async function initUser() {
  // 检查 localStorage 是否已有用户信息
  const storedUser = localStorage.getItem('forumUser')
  if (storedUser) return

  // 开发环境使用模拟数据
  if (isDev.value && !isWxWork.value) {
    const mockUser = getMockUser()
    try {
      const res = await syncUser(mockUser)
      const userData = res.data || mockUser
      localStorage.setItem('forumUser', JSON.stringify(userData))
    } catch (e) {
      console.error('同步用户失败', e)
    }
  }
}

// 加载帖子详情
async function loadPost() {
  try {
    const res = await getPostDetail(route.params.id)
    post.value = res.data
    loadComments()
    // 检查关注状态
    loadFollowStatus()
  } catch (e) {
    showToast('加载失败')
  }
}

// 检查用户是否已关注该帖子
async function loadFollowStatus() {
  const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
  if (!user.wxUserid) return

  try {
    const res = await checkFollow(route.params.id, user.wxUserid)
    isFollowed.value = res.data === true
  } catch (e) {
    console.error('检查关注状态失败', e)
  }
}

// 关注/取消关注帖子
async function toggleFollow() {
  const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
  if (!user.wxUserid) {
    showToast('请先登录')
    return
  }

  try {
    if (isFollowed.value) {
      await unfollowPost({ wxUserid: user.wxUserid, postId: post.value.postId })
      isFollowed.value = false
      showToast('已取消关注')
    } else {
      await followPost({ wxUserid: user.wxUserid, postId: post.value.postId })
      isFollowed.value = true
      showToast('关注成功')
    }
  } catch (e) {
    showToast('操作失败')
  }
}

// 确认删除帖子
async function confirmDelete() {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '删除后无法恢复，确定要删除这篇帖子吗？',
    })
    
    // 用户确认删除
    const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
    if (!user.wxUserid) {
      showToast('请先登录')
      return
    }
    
    await deletePost({ wxUserid: user.wxUserid, postId: post.value.postId })
    showToast('删除成功')
    
    // 返回上一页
    router.back()
  } catch (e) {
    // 用户取消或请求失败
    if (e !== 'cancel') {
      console.error('删除失败', e)
      showToast('删除失败')
    }
  }
}

// 加载评论
async function loadComments() {
  if (commentFinished.value) return
  
  commentLoading.value = true
  try {
    const res = await getCommentList(route.params.id, { 
      pageNum: commentPageNum.value, 
      pageSize: 20 
    })
    const rows = res.rows || []
    
    if (commentPageNum.value === 1) {
      comments.value = rows
    } else {
      comments.value.push(...rows)
    }
    
    if (rows.length < 20) {
      commentFinished.value = true
    } else {
      commentPageNum.value++
    }
  } catch (e) {
    console.error('加载评论失败', e)
  } finally {
    commentLoading.value = false
  }
}

// 提交评论
async function submitComment() {
  if (!commentText.value.trim()) {
    showToast('请输入评论内容')
    return
  }

  showToast({
    type: 'loading',
    message: '发布中...',
    forbidClick: true
  })

  try {
    const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
    if (!user.wxUserid) {
      showToast('用户信息获取失败')
      return
    }
    
    await createComment({
      postId: post.value.postId,
      wxUserid: user.wxUserid,  // 后端需要 wxUserid
      content: commentText.value,
    })
    
    showToast('评论成功')
    commentText.value = ''
    showEmojiPicker.value = false // 发送成功后关闭表情面板
    
    // 刷新评论列表
    commentPageNum.value = 1
    commentFinished.value = false
    comments.value = [] // 必须清空，否则 vant list 会有问题
    await loadComments() 
    
    // 更新帖子评论数
    if (post.value) {
      post.value.commentCount = (post.value.commentCount || 0) + 1
    }
  } catch (e) {
    showToast('评论失败')
  }
}

// 切换表情面板
function toggleEmojiPicker() {
  showEmojiPicker.value = !showEmojiPicker.value
  // 打开面板时刷新最近使用列表
  if (showEmojiPicker.value) {
    recentEmojis.value = getRecentEmojis()
  }
}

// 插入表情（插入 [表情名] 格式，保持面板打开）
function insertEmoji(emoji) {
  commentText.value += `[${emoji.name}]`
  // 保存到最近使用 (localStorage)，但不刷新UI列表
  addRecentEmoji(emoji)
}

// 删除表情或字符
function deleteEmojiOrChar() {
  const text = commentText.value
  if (!text) return

  // 尝试匹配末尾的 [表情名]
  const emojiRegex = /\[[^\]]+\]$/
  const match = text.match(emojiRegex)

  if (match) {
    // 如果末尾是表情，删除整个表情标签
    commentText.value = text.slice(0, -match[0].length)
  } else {
    // 否则删除最后一个字符
    commentText.value = text.slice(0, -1)
  }
}

// 预览图片
function previewImage(index) {
  showImagePreview({
    images: images.value,
    startPosition: index
  })
}

// 分享
function handleShare() {
  const url = window.location.href
  shareToUsers(post.value?.title, url)
}

// 返回
function goBack() {
  router.back()
}

// 格式化时间 (显示 HH:mm 或日期)
function formatTimeDayTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  // 如果是当天，只显示时间 HH:mm
  const today = new Date()
  if (date.toDateString() === today.toDateString()) {
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${hours}:${minutes}`
  }
  // 否则显示日期 M-d
  return `${date.getMonth() + 1}-${date.getDate()}`
}

function formatTime(timeStr) {
    if (!timeStr) return ''
    return timeStr.substring(0, 16)
}
</script>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background: var(--bg-color);
  padding-bottom: 70px;
}

.access-denied {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  color: var(--text-secondary);
}

.post-content-wrap {
  background: #fff;
  padding: 16px;
  margin-bottom: 10px;
}

@media (prefers-color-scheme: dark) {
  .post-content-wrap,
  .comment-section,
  .comment-input-bar {
    background: #2c2c2c;
  }
}



.post-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
  line-height: 1.4;
}

.post-body {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-primary);
  white-space: pre-wrap;
  margin-bottom: 16px;
}

.post-images {
  margin-top: 16px;
  margin-bottom: 20px;
}

.post-image {
  margin-bottom: 8px;
  border-radius: 4px;
}

/* Meta Footer */
.post-meta-footer {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #999;
  margin-top: 24px;
  margin-bottom: 12px;
}

.meta-author {
  color: #576b95; /* 蓝色名字 */
  font-weight: 500;
}

.meta-separator {
  margin: 0 6px;
  color: #ccc;
}

.delete-btn {
  color: #576b95;
  margin-left: 10px;
  cursor: pointer;
}

.locked {
  color: #ee0a24;
}

@media (prefers-color-scheme: dark) {
  .meta-author, .delete-btn {
    color: #7d90a9;
  }
}

/* Stats Row */
.post-stats-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid #f5f5f5;
  color: #999;
  font-size: 14px;
}

@media (prefers-color-scheme: dark) {
  .post-stats-row {
    border-top: 1px solid #333;
    color: #666;
  }
}

.stats-left {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-item .van-icon {
  font-size: 18px; 
}

.star-btn {
  color: #999;
  cursor: pointer;
}

.star-btn.followed {
  color: #ffc107;
}


.comment-section {
  background: var(--bg-color); /* 评论区背景改为灰色，与内容区分 */
  padding: 0;
  min-height: 200px;
}

.comment-item {
  background: #fff;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
}

@media (prefers-color-scheme: dark) {
  .comment-item {
    background: #2c2c2c;
  }
}

.comment-content {
  flex: 1;
  margin-left: 10px;
}

.comment-header {
  display: flex;
  align-items: center; /* 左对齐名字和tag */
}

.comment-nickname {
  font-size: 14px;
  font-weight: 500;
  color: #333; /* 黑色名字 */
}

@media (prefers-color-scheme: dark) {
  .comment-nickname {
    color: #e5e5e5;
  }
}

.landlord-tag {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.comment-meta {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  display: flex;
  align-items: center;
}

.meta-dot {
  margin: 0 4px;
}

.comment-text {
  font-size: 15px;
  color: var(--text-primary);
  margin-top: 4px;
  line-height: 1.5;
}

/* 空状态样式 */
.no-comments {
  text-align: center;
  padding: 80px 0;
  color: #999;
  font-size: 15px;
  background: var(--bg-color);
}


/* 底部评论操作区整体容器 */
.comment-action-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 100;
  border-top: 1px solid #eee;
}

@media (prefers-color-scheme: dark) {
  .comment-action-area {
    background: #2c2c2c;
    border-top: 1px solid #333;
  }
}

/* 输入栏 */
.comment-input-bar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  gap: 12px;
}

.input-wrap {
  flex: 1;
  min-height: 36px; /* 默认高度 */
  display: flex;
  align-items: center;
  padding: 4px 12px;
  background: #f7f7f7; /* 默认灰色背景 */
  border-radius: 18px; /* 圆润的边角 */
  transition: all 0.2s; /* 平滑过渡 */
}

/* 展开状态：类似多行文本域 */
.input-wrap.expanded {
  min-height: 100px;
  align-items: flex-start; /* 文字顶对齐 */
  padding: 8px 12px;
  background: #f2f3f5; /* 稍微深一点的背景 */
  border-radius: 8px; /* 方一点的圆角 */
}

.native-textarea {
  width: 100%;
  border: none;
  background: transparent;
  padding: 0;
  margin: 0;
  font-size: 15px;
  color: #333;
  resize: none;
  outline: none;
  line-height: 1.4;
  /* 确保在iOS上可点击 */
  -webkit-user-select: text;
  user-select: text;
}

@media (prefers-color-scheme: dark) {
  .input-wrap {
    background: #3a3a3a;
  }
  .native-textarea {
    color: #e5e5e5;
  }
}

/* 表情面板容器 */
.emoji-picker-panel {
  position: relative;
  height: 300px;
  background: #fff;
  border-top: 1px solid #eee;
}

@media (prefers-color-scheme: dark) {
  .emoji-picker-panel {
    background: #2c2c2c;
    border-top: 1px solid #333;
  }
}

.emoji-picker {
  height: 100%;
  padding: 12px;
  overflow-y: auto;
  padding-bottom: 60px; /* 留出底部按钮空间 */
}

.emoji-section {
  margin-bottom: 16px;
}

.emoji-section .section-title {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
  padding-left: 4px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.emoji-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  cursor: pointer;
  border-radius: 6px;
}

.emoji-item:active {
  background: #f0f0f0;
}

.emoji-item img {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

@media (prefers-color-scheme: dark) {
  .emoji-item:active {
    background: #333;
  }
}

/* 悬浮操作按钮区 */
.emoji-actions {
  position: absolute;
  bottom: 20px;
  right: 16px;
  display: flex;
  gap: 12px;
  z-index: 101;
}

.action-btn {
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

@media (prefers-color-scheme: dark) {
  .action-btn {
    background: #3a3a3a;
  }
}

/* 删除按钮 */
.action-btn.delete-btn {
  width: 44px;
  color: #333;
}

@media (prefers-color-scheme: dark) {
  .action-btn.delete-btn {
    color: #ccc;
  }
}

/* 发送按钮 */
.send-btn {
  padding: 0 20px;
  background: #1989fa; /* 蓝色主色 */
  color: #fff;
  font-weight: 500;
}

/* 发送按钮禁用态 */
.send-btn.disabled {
  background: #f0f0f0;
  color: #ccc;
  box-shadow: none;
}

@media (prefers-color-scheme: dark) {
  .send-btn.disabled {
    background: #3a3a3a;
    color: #666;
    border: 1px solid #444;
  }
}
</style>
