<template>
  <div class="post-detail-page">
    
    <!-- 非企业微信环境提示 -->
    <div v-if="!isWxWork && !isDev" class="access-denied">
      <van-icon name="warning-o" size="60" />
      <p>{{ accessDeniedMessage }}</p>
    </div>

    <!-- 限流/不存在的帖子：显示纯空白页（无任何 UI） -->
    <div v-else-if="!postLoading && !post" class="restricted-blank-page"></div>

    <!-- 正常内容 -->
    <template v-else>
      <!-- 顶部导航 -->
      <van-nav-bar 
        title="详情" 
        fixed 
        placeholder
        class="nav-bar-dark"
      >
        <template #right>
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" @click="handleShare" style="cursor: pointer;">
            <path d="M12 2v12M12 2l4 4M12 2l-4 4"/>
            <path d="M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8"/>
          </svg>
        </template>
      </van-nav-bar>

      <!-- 加载中骨架屏 -->
      <div v-if="postLoading" class="post-skeleton">
        <van-skeleton title title-width="60%" :row="4" />
        <div class="skeleton-meta">
          <van-skeleton-paragraph row-width="40%" />
        </div>
      </div>

      <!-- 帖子内容 -->
      <div v-else-if="post" class="post-content-wrap">
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

        <!-- 视频展示 -->
        <div v-if="post.videoUrl" class="post-video">
          <!-- 可嵌入的视频 -->
          <div v-if="getEmbedUrl(post.videoUrl)" class="video-embed-wrapper">
            <iframe 
              :src="getEmbedUrl(post.videoUrl)" 
              frameborder="0" 
              allowfullscreen
              class="video-iframe"
            ></iframe>
          </div>
          <!-- 无法嵌入时显示跳转卡片 -->
          <div v-else class="video-card" @click="openVideo(post.videoUrl)">
            <div class="video-icon">
              <van-icon name="play-circle-o" size="40" color="#fff" />
            </div>
            <div class="video-info">
              <span class="video-platform">{{ getVideoPlatform(post.videoUrl) }}</span>
              <span class="video-text">点击播放视频</span>
            </div>
          </div>
        </div>

        <!-- 底部元信息 (作者 · 时间 · 删除 | 分类右对齐) -->
        <div class="post-meta-footer">
          <div class="meta-left">
            <span class="meta-author" @click="handleOpenUserProfile(post.user?.wxUserid)" style="cursor: pointer;">{{ post.user?.nickname || '匿名用户' }}</span>
            <span class="meta-separator">·</span>
            <span class="meta-time">{{ formatTime(post.createTime) }}</span>
            <span v-if="post.isLocked === '1'" class="locked">
              <span class="meta-separator">·</span>
              <van-icon name="lock" /> 已锁定
            </span>
            <!-- 编辑按钮，作者且10分钟内可编辑 -->
            <span class="edit-btn" v-if="canEdit" @click="goToEdit">编辑</span>
            <!-- 删除按钮，作者或管理员可删除 -->
            <span class="delete-btn" v-if="canDelete" @click="confirmDelete">删除</span>
          </div>
          <span v-if="post.categoryName" class="meta-category">{{ post.categoryName }}</span>
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
              @click="handleOpenUserProfile(comment.user?.wxUserid)"
              style="cursor: pointer;"
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
                <!-- 删除按钮：仅评论作者可见 -->
                <span v-if="canDeleteComment(comment)" class="comment-delete-btn" @click="confirmDeleteComment(comment)">删除</span>
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
      <div v-if="post?.isLocked !== '1'" class="comment-action-area" :style="{ bottom: bottomOffset + 'px' }">
        <!-- 输入栏 -->
        <div class="comment-input-bar">
          <textarea
            v-model="commentText"
            :rows="isInputExpanded ? 5 : 2"
            placeholder="发表评论..."
            class="comment-textarea"
            ref="commentInputRef"
            enterkeyhint="send"
            @focus="isFocused = true; showEmojiPicker = false"
            @blur="handleBlur"
            @keydown.enter.prevent="handleEnterKey"
          ></textarea>
          <van-icon 
            :name="showEmojiPicker ? 'smile' : 'smile-o'" 
            size="24" 
            class="emoji-toggle-icon"
            :class="{ active: showEmojiPicker }"
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
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showImagePreview, showConfirmDialog } from 'vant'
import { getPostDetail, getCommentList, createComment, deleteComment, syncUser, checkFollow, followPost, unfollowPost, deletePost } from '@/api/forum'
import { isWxWorkEnv, getAccessDeniedMessage, shareToUsers, openUserProfile, getMockUser } from '@/utils/wxwork'
import { emojiList, emojiBasePath, renderEmojis, getRecentEmojis, addRecentEmoji } from '@/config/emojis'

const route = useRoute()
const router = useRouter()

// 状态
const isWxWork = ref(true)
const isDev = ref(import.meta.env.DEV)
const accessDeniedMessage = ref(getAccessDeniedMessage())
const post = ref(null)
const postLoading = ref(true)
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
const debugInfo = ref('') // 调试信息
const bottomOffset = ref(0) // 评论区域底部偏移（键盘高度）

// 记录锁定的键盘高度和初始 offsetTop
let lockedKeyboardHeight = 0
let initialOffsetTop = 0

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

// 处理回车键（发送评论）
function handleEnterKey() {
  // 有内容时发送，没有内容时不做任何操作
  if (commentText.value.trim()) {
    submitComment()
  }
}

// 最近使用的表情
const recentEmojis = ref(getRecentEmojis())

// 监听软键盘高度变化
function handleKeyboardResize() {
  if (window.visualViewport) {
    const keyboardHeight = window.innerHeight - window.visualViewport.height
    const offsetTop = window.visualViewport.offsetTop
    
    if (keyboardHeight > 50) {
      // 键盘弹出状态
      if (lockedKeyboardHeight === 0) {
        // 首次弹出或从收起状态重新弹出，锁定高度和初始 offsetTop
        lockedKeyboardHeight = keyboardHeight
        initialOffsetTop = offsetTop
      }
      // 计算 offsetTop 的变化量（相对于初始值）
      const offsetDelta = offsetTop - initialOffsetTop
      // 使用锁定的高度减去变化量
      bottomOffset.value = Math.max(0, lockedKeyboardHeight - offsetDelta)
    } else {
      // 键盘收起，重置所有锁定值
      lockedKeyboardHeight = 0
      initialOffsetTop = 0
      bottomOffset.value = 0
    }
  }
}

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

// 10分钟编辑时间窗口（毫秒）
const EDIT_TIME_WINDOW_MS = 10 * 60 * 1000

// 计算是否可以编辑（作者且发布10分钟内）
const canEdit = computed(() => {
  if (!isAuthor.value || !post.value?.createTime) return false
  const elapsed = Date.now() - new Date(post.value.createTime).getTime()
  return elapsed < EDIT_TIME_WINDOW_MS
})

// 跳转到编辑页面
function goToEdit() {
  router.push(`/post/edit/${post.value.postId}`)
}

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
  // 禁止 body 滚动（与发帖页面保持一致，防止键盘弹出时视口变化）
  document.body.style.overflow = 'hidden'
  document.body.style.height = '100%'
  document.documentElement.style.overflow = 'hidden'
  document.documentElement.style.height = '100%'
  
  isWxWork.value = isWxWorkEnv()
  
  if (isDev.value || isWxWork.value) {
    await initUser()
    await loadPost()
  }
  
  // 调试：监控关键信息
  const debugWidths = () => {
    const vw = window.innerWidth
    const vvw = window.visualViewport?.width || 'N/A'
    const vvLeft = window.visualViewport?.offsetLeft || 0
    const scrollX = window.scrollX || window.pageXOffset || 0
    const bodyScrollLeft = document.body.scrollLeft
    const docScrollLeft = document.documentElement.scrollLeft
    const pageScrollLeft = document.querySelector('.post-detail-page')?.scrollLeft || 0
    
    // 评论框位置
    const actionArea = document.querySelector('.comment-action-area')
    const actionRect = actionArea?.getBoundingClientRect()
    const actionLeft = actionRect?.left || 'N/A'
    
    let info = `视口: ${vw}px | VP: ${vvw}px\n`
    info += `vpOffsetLeft: ${vvLeft}px\n`
    info += `scrollX: ${scrollX}px\n`
    info += `bodyScrollL: ${bodyScrollLeft}px\n`
    info += `docScrollL: ${docScrollLeft}px\n`
    info += `pageScrollL: ${pageScrollLeft}px\n`
    info += `actionLeft: ${actionLeft}px`
    
    debugInfo.value = info
  }
  
  // 监听 visualViewport 变化，动态调整所有容器宽度
  const updateWidthsForViewport = () => {
    if (window.visualViewport) {
      const vpWidth = window.visualViewport.width
      // 调整页面容器和评论区域（不调整 body/html）
      const page = document.querySelector('.post-detail-page')
      const actionArea = document.querySelector('.comment-action-area')
      if (page) page.style.width = `${vpWidth}px`
      if (actionArea) actionArea.style.width = `${vpWidth}px`
      // 调整导航栏（Vant组件）
      document.querySelectorAll('.van-nav-bar, .van-nav-bar__content').forEach(el => {
        el.style.width = `${vpWidth}px`
      })
    }
  }
  
  if (window.visualViewport) {
    window.visualViewport.addEventListener('resize', updateWidthsForViewport)
    window.visualViewport.addEventListener('scroll', updateWidthsForViewport)
    // 监听键盘高度变化
    window.visualViewport.addEventListener('resize', handleKeyboardResize)
    window.visualViewport.addEventListener('scroll', handleKeyboardResize)
  }
  
  // 监听 focus 事件
  const textarea = document.querySelector('.comment-textarea')
  if (textarea) {
    textarea.addEventListener('focus', () => {
      debugWidths()
      // 延迟再次检查（键盘弹出后）
      setTimeout(debugWidths, 500)
    })
  }
})

onUnmounted(() => {
  // 恢复 body 样式
  document.body.style.overflow = ''
  document.body.style.height = ''
  document.body.style.width = ''
  document.documentElement.style.overflow = ''
  document.documentElement.style.height = ''
  document.documentElement.style.width = ''
  
  // 移除 visualViewport 事件监听
  if (window.visualViewport) {
    window.visualViewport.removeEventListener('resize', handleKeyboardResize)
    window.visualViewport.removeEventListener('scroll', handleKeyboardResize)
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
    const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
    const res = await getPostDetail(route.params.id, user.wxUserid)
    // 检查是否返回了错误（限流帖子非作者访问）
    if (res.code !== 200) {
      post.value = null
      // 不显示任何提示，直接显示空白页
      postLoading.value = false
      return
    }
    post.value = res.data
    // van-list 会在组件挂载时自动触发 load，不需要手动调用 loadComments
    // 检查关注状态
    loadFollowStatus()
  } catch (e) {
    post.value = null
    // 不显示任何提示
  } finally {
    postLoading.value = false
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

// 视频平台识别
const VIDEO_PLATFORMS = [
  { name: '优酷', pattern: /youku\.com|v\.youku\.com|m\.youku\.com/i },
  { name: '腾讯视频', pattern: /v\.qq\.com|m\.v\.qq\.com|qq\.com\/x\/cover/i },
  { name: 'B站', pattern: /bilibili\.com|b23\.tv/i }
]

function getVideoPlatform(url) {
  if (!url) return '视频'
  for (const p of VIDEO_PLATFORMS) {
    if (p.pattern.test(url)) {
      return p.name
    }
  }
  return '视频'
}

// 打开视频链接
function openVideo(url) {
  if (!url) return
  window.open(url, '_blank')
}

// 获取嵌入播放URL (返回null表示无法嵌入)
function getEmbedUrl(url) {
  if (!url) return null
  
  // B站 - 支持 bilibili.com/video/BVxxx 格式
  const bvMatch = url.match(/bilibili\.com\/video\/(BV[\w]+)/i)
  if (bvMatch) {
    return `//player.bilibili.com/player.html?bvid=${bvMatch[1]}&high_quality=1&danmaku=0`
  }
  
  // B站 - 支持 bilibili.com/video/avxxx 格式
  const avMatch = url.match(/bilibili\.com\/video\/av(\d+)/i)
  if (avMatch) {
    return `//player.bilibili.com/player.html?aid=${avMatch[1]}&high_quality=1&danmaku=0`
  }
  
  // 腾讯视频 - 支持 v.qq.com/x/cover/xxx/xxx.html 或 v.qq.com/x/page/xxx.html
  const qqMatch = url.match(/v\.qq\.com\/x\/(?:cover\/[\w]+\/|page\/)([\w]+)\.html/i)
  if (qqMatch) {
    return `https://v.qq.com/txp/iframe/player.html?vid=${qqMatch[1]}`
  }
  
  // 腾讯视频移动端 - 支持 m.v.qq.com/play/play.html?vid=xxx
  const qqMobileMatch = url.match(/m\.v\.qq\.com\/.*[?&]vid=([^&]+)/i)
  if (qqMobileMatch) {
    return `https://v.qq.com/txp/iframe/player.html?vid=${qqMobileMatch[1]}`
  }
  
  // 优酷 - 支持 v.youku.com/v_show/id_xxx.html
  const youkuMatch = url.match(/v\.youku\.com\/v_show\/id_([\w=]+)/i)
  if (youkuMatch) {
    return `https://player.youku.com/embed/${youkuMatch[1]}`
  }
  
  // 优酷移动端 - 支持 m.youku.com/mid_video/id_xxx.html
  const youkuMobileMatch = url.match(/m\.youku\.com\/.*\/id_([\w=]+)/i)
  if (youkuMobileMatch) {
    return `https://player.youku.com/embed/${youkuMobileMatch[1]}`
  }
  
  // 短链接和其他格式无法嵌入，返回null
  return null
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
    // 过滤重复数据 (应对后端分页失效返回相同数据的情况)
    const newRows = rows.filter(item => 
      !comments.value.some(existing => existing.commentId === item.commentId)
    )

    if (commentPageNum.value === 1) {
      comments.value = rows
    } else {
      comments.value.push(...newRows)
    }
    
    // 如果没有新数据，或者返回数据少于页大小，或者是第一页且总数匹配，则结束
    const total = res.total || 0
    if (newRows.length === 0 || rows.length < 20 || (total > 0 && comments.value.length >= total)) {
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

  // 关闭键盘
  if (commentInputRef.value) {
    commentInputRef.value.blur()
  }
  isFocused.value = false
  showEmojiPicker.value = false

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
      userUnit: user.unit || '',
      userDept: user.department || ''
    })
    
    // 在屏幕中间显示评论成功
    showToast({
      message: '评论成功',
      position: 'middle'
    })
    
    // 本地构造新评论对象，直接追加到列表末尾（无需重新请求API）
    // 楼层号 = 当前列表中最大楼层号 + 1（考虑到已删除的评论仍占用楼层）
    const maxFloor = comments.value.length > 0 
      ? Math.max(...comments.value.map(c => c.floorNum || 0))
      : 0
    const newComment = {
      commentId: Date.now(), // 临时ID，下次刷新时会用真实ID替换
      postId: post.value.postId,
      content: commentText.value,
      createTime: new Date().toISOString(),
      floorNum: maxFloor + 1, // 楼层号 = 最大楼层号 + 1
      user: user, // 当前用户信息
      userId: user.userId // 用于判断是否是楼主
    }
    comments.value.push(newComment)
    
    // 清空输入框
    commentText.value = ''
    
    // 更新帖子评论数
    if (post.value) {
      post.value.commentCount = (post.value.commentCount || 0) + 1
    }
    
    // 滚动到新评论（使用 nextTick 确保 DOM 更新）
    await nextTick()
    setTimeout(() => {
      const page = document.querySelector('.post-detail-page')
      const allComments = document.querySelectorAll('.comment-item')
      const lastComment = allComments.length > 0 ? allComments[allComments.length - 1] : null
      
      if (page && lastComment) {
        const pageRect = page.getBoundingClientRect()
        const commentRect = lastComment.getBoundingClientRect()
        const currentScrollTop = page.scrollTop
        const targetScrollTop = currentScrollTop + commentRect.top - pageRect.top - 100
        page.scrollTo({ top: targetScrollTop, behavior: 'smooth' })
      }
    }, 100)
  } catch (e) {
    showToast({
      message: '评论失败',
      position: 'middle'
    })
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
  // 显示短暂 loading（企微分享调用后会立即返回）
  showToast({
    type: 'loading',
    message: '正在打开...',
    forbidClick: true,
    duration: 800  // 800ms 后自动关闭
  })
  const url = window.location.href
  shareToUsers(post.value?.title, url)
}

// 点击头像打开用户个人信息页
function handleOpenUserProfile(wxUserid) {
  if (!wxUserid) {
    showToast('无法获取用户信息')
    return
  }
  // 显示短暂 loading（企微 openUserProfile 调用后会立即返回，不会等待用户从原生页面返回）
  showToast({
    type: 'loading',
    message: '正在打开...',
    forbidClick: true,
    duration: 800  // 800ms 后自动关闭
  })
  openUserProfile(wxUserid)
}

// 判断当前用户是否可以删除该评论（仅评论作者可删）
function canDeleteComment(comment) {
  const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
  if (!user.wxUserid) return false
  // 评论作者可以删除自己的评论
  return comment.user?.wxUserid === user.wxUserid
}

// 确认删除评论
async function confirmDeleteComment(comment) {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '删除后无法恢复，确定要删除这条评论吗？',
    })
    
    // 用户确认删除
    const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
    if (!user.wxUserid) {
      showToast('请先登录')
      return
    }
    
    showToast({ type: 'loading', message: '删除中...', forbidClick: true })
    
    await deleteComment({ 
      commentId: comment.commentId, 
      wxUserid: user.wxUserid  // 记录删除者
    })
    
    showToast('删除成功')
    
    // 从本地列表中移除该评论
    const index = comments.value.findIndex(c => c.commentId === comment.commentId)
    if (index > -1) {
      comments.value.splice(index, 1)
    }
    
    // 更新帖子评论数
    if (post.value && post.value.commentCount > 0) {
      post.value.commentCount--
    }
  } catch (e) {
    // 用户取消或请求失败
    if (e !== 'cancel') {
      console.error('删除评论失败', e)
      showToast('删除失败')
    }
  }
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
  position: fixed;
  top: 0;
  left: 0;
  width: 100%; /* 使用 100% 而非 left/right: 0 */
  height: 100%;
  background: var(--bg-color);
  overflow-x: hidden;
  overflow-y: auto;
  box-sizing: border-box;
}

/* 调试面板 */
.debug-panel {
  position: fixed;
  top: 50px;
  left: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.9);
  color: #0f0;
  padding: 12px;
  border-radius: 8px;
  z-index: 9999;
  font-size: 12px;
}
.debug-panel pre {
  margin: 0;
  white-space: pre-wrap;
}
.debug-panel small {
  color: #999;
  display: block;
  margin-top: 8px;
}

/* 导航栏深色主题 */
@media (prefers-color-scheme: dark) {
  .nav-bar-dark {
    background: #191919 !important;
  }
  .nav-bar-dark :deep(.van-nav-bar__title) {
    color: #f5f5f5 !important;
  }
  .nav-bar-dark :deep(.van-nav-bar__arrow) {
    color: #f5f5f5 !important;
  }
  .nav-bar-dark :deep(svg) {
    stroke: #f5f5f5 !important;
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

/* 骨架屏样式 */
.post-skeleton {
  padding: 16px;
  padding-top: 20px;
  background: #fff;
  min-height: 200px;
}

.skeleton-meta {
  margin-top: 20px;
}

@media (prefers-color-scheme: dark) {
  .post-skeleton {
    background: #2c2c2c;
  }
}

.post-content-wrap {
  background: #fff;
  padding: 16px;
  padding-top: 20px; /* 确保不被导航栏遮挡 */
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
  border-radius: 8px;
  margin-bottom: 10px;
}

/* Video Card */
.post-video {
  margin-top: 12px;
  margin-bottom: 12px;
}

/* 响应式视频嵌入 */
.video-embed-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 56.25%; /* 16:9 比例 */
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.video-iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: none;
}

.video-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  cursor: pointer;
}

.video-icon {
  margin-right: 16px;
}

.video-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.video-platform {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.video-text {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.video-card:active {
  opacity: 0.9;
}

/* Meta Footer */
.post-meta-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #999;
  margin-top: 24px;
  margin-bottom: 12px;
}

.meta-left {
  display: flex;
  align-items: center;
}

.meta-author {
  color: #576b95; /* 蓝色名字 */
  font-weight: 500;
}

.meta-separator {
  margin: 0 6px;
  color: #ccc;
}

.meta-category {
  font-size: 12px;
  color: #1989fa;
  background: #e6f4ff;
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
}

.delete-btn {
  color: #576b95;
  margin-left: 10px;
  cursor: pointer;
}

.edit-btn {
  color: #1989fa;
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
  .meta-category {
    color: #1989fa;
    background: #1a3050;
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
  padding-bottom: 80px; /* 给固定的评论输入框留出空间 */
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

.comment-delete-btn {
  color: #576b95;
  font-size: 12px;
  cursor: pointer;
  margin-left: 10px;
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
  width: 100%; /* 使用 100% 而非 100vw，避免滚动条导致溢出 */
  overflow: hidden;
  background: #fff;
  z-index: 100;
  border-top: 1px solid #eee;
  box-sizing: border-box;
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
  padding: 12px 16px;
  gap: 12px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden; /* 防止子元素溢出 */
}

/* 评论输入框 - 简洁设计，模仿真实系统 */
.comment-textarea {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 15px;
  color: #333;
  resize: none;
  outline: none;
  line-height: 1.5;
  padding: 0;
  -webkit-user-select: text;
  user-select: text;
}

.comment-textarea::placeholder {
  color: #999;
}

@media (prefers-color-scheme: dark) {
  .comment-textarea {
    color: #e5e5e5;
  }
  .comment-textarea::placeholder {
    color: #666;
  }
}

/* 表情切换图标 */
.emoji-toggle-icon {
  color: #666;
  cursor: pointer;
}
.emoji-toggle-icon.active {
  color: #1989fa;
}
@media (prefers-color-scheme: dark) {
  .emoji-toggle-icon {
    color: #999;
  }
  .emoji-toggle-icon.active {
    color: #4da3ff;
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

<!-- 全局样式：导航栏深色主题 -->
<style>
@media (prefers-color-scheme: dark) {
  .post-detail-page .van-nav-bar {
    background: #191919 !important;
  }
  .post-detail-page .van-nav-bar__title {
    color: #f5f5f5 !important;
  }
  .post-detail-page .van-nav-bar__arrow {
    color: #f5f5f5 !important;
  }
  .post-detail-page .van-nav-bar__right svg {
    stroke: #f5f5f5 !important;
  }
  .post-detail-page .van-nav-bar__placeholder {
    background: #191919 !important;
  }
}

/* 限流/不存在帖子的空白页 */
.restricted-blank-page {
  min-height: 100vh;
  background: #f7f8fa;
}

@media (prefers-color-scheme: dark) {
  .restricted-blank-page {
    background: #000;
  }
}
</style>

<!-- 全局样式（非作用域）- 强制禁止横向滚动 -->
<style>
html, body {
  overflow-x: hidden !important;
  max-width: 100vw !important;
  touch-action: pan-y !important; /* 只允许纵向触摸滚动 */
  overscroll-behavior-x: none !important; /* 禁止横向过度滚动 */
}
</style>
