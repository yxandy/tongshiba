<template>
  <div class="post-create-page">
    <!-- 上传进度遮罩 -->
    <div v-if="submitting" class="upload-overlay">
      <div class="upload-progress-container">
        <div class="progress-circle">
          <svg viewBox="0 0 100 100">
            <circle class="progress-bg" cx="50" cy="50" r="45" />
            <circle 
              class="progress-bar" 
              cx="50" cy="50" r="45"
              :style="{ strokeDashoffset: progressOffset }"
            />
          </svg>
          <span class="progress-text">{{ uploadProgress }}%</span>
        </div>
        <p class="upload-hint">{{ uploadProgress < 100 ? '正在上传...' : '处理中...' }}</p>
      </div>
    </div>

    <!-- 表单内容 -->
    <div class="form-content">
      <!-- 分类选择（必选） -->
      <div class="category-picker">
        <div class="category-header">
          <span class="category-label">选择分类</span>
          <span class="required-mark">*</span>
          <span v-if="!selectedCategory" class="category-hint">请选择一个分类</span>
        </div>
        <div class="category-scroll">
          <span 
            v-for="cat in categories" 
            :key="cat.categoryId"
            class="category-tag"
            :class="{ active: selectedCategory === cat.categoryId }"
            @click="selectedCategory = cat.categoryId"
          >
            {{ cat.name }}
          </span>
        </div>
      </div>

      <van-field
        ref="titleInputRef"
        v-model="title"
        placeholder="主题"
        :border="false"
        class="title-input"
        maxlength="50"
        @focus="onInputFocus"
      />
      <van-field
        v-model="content"
        type="textarea"
        placeholder="内容"
        :border="false"
        rows="10"
        autosize
        class="content-input"
        maxlength="2000"
        @focus="onInputFocus"
      />

      <!-- 已选图片预览 -->
      <div v-if="imageList.length > 0" class="image-preview">
        <div 
          v-for="(img, idx) in imageList" 
          :key="idx" 
          class="preview-item"
        >
          <van-image width="80" height="80" fit="cover" :src="img" />
          <van-icon name="clear" class="remove-btn" @click="removeImage(idx)" />
        </div>
      </div>

      <!-- 已添加视频预览 -->
      <div v-if="videoUrl" class="video-preview">
        <div class="video-info">
          <van-icon name="video-o" size="20" />
          <span class="video-platform">{{ getVideoPlatform(videoUrl) }}</span>
          <span class="video-url">{{ truncateUrl(videoUrl) }}</span>
          <van-icon name="cross" class="remove-video" @click="removeVideo" />
        </div>
      </div>
    </div>

    <!-- 底部功能区固定容器 -->
    <div class="bottom-container" :style="{ bottom: bottomOffset + 'px' }">
      <!-- 底部工具栏 -->
      <div class="toolbar">
        <!-- 左侧工具组 -->
        <div class="left-tools">
          <!-- 关闭 -->
          <div class="tool-icon close-btn" @click="goBack">
            <van-icon name="cross" />
          </div>
          
          <!-- 图片 -->
          <div class="tool-icon" @click="selectImage">
            <van-icon name="photo-o" />
          </div>
          
          <!-- 视频 -->
          <div class="tool-icon" @click="showVideoDialog = true">
            <van-icon name="video-o" />
          </div>
          
          <!-- 表情 -->
          <div class="tool-icon" @click="toggleEmojiPicker">
            <van-icon :name="showEmojiPicker ? 'smile' : 'smile-o'" :color="showEmojiPicker ? '#1989fa' : ''" />
          </div>
        </div>

        <!-- 右侧发送 -->
        <div 
          class="send-btn" 
          :class="{ active: canSubmit && !submitting, disabled: !canSubmit || submitting }"
          @click="submitPost"
        >
          <van-icon v-if="submitting" name="loading" class="spinner" />
          <van-icon v-else name="arrow-up" />
        </div>
      </div>

      <!-- 表情选择面板（内联式） -->
      <div v-show="showEmojiPicker" class="emoji-picker-panel">
        <div class="emoji-picker-content">
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
        <!-- 删除按钮 -->
        <div class="emoji-actions">
          <div class="action-btn delete-btn" @click="deleteEmojiOrChar">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" style="display: block;">
            <path d="M22 3H7c-.69 0-1.23.35-1.59.88L0 12l5.41 8.11c.36.53.9.89 1.59.89h15c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-3 12.59L17.59 17 14 13.41 10.41 17 9 15.59 12.59 12 9 8.41 10.41 7 14 10.59 17.59 7 19 8.41 15.41 12 19 15.59z"/>
          </svg>
        </div>
        </div>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input 
      ref="fileInput" 
      type="file" 
      accept="image/*" 
      multiple 
      style="display: none" 
      @change="handleImageSelect"
    />

    <!-- 视频链接输入弹窗 -->
    <van-dialog
      v-model:show="showVideoDialog"
      title="插入视频链接"
      show-cancel-button
      :before-close="onVideoDialogClose"
      :lazy-render="false"
      @opened="onVideoDialogOpened"
    >
      <div class="video-dialog-content">
        <p class="video-tips">支持：优酷、腾讯视频、B站</p>
        <van-field
          ref="videoInputRef"
          v-model="videoInputUrl"
          placeholder="粘贴视频链接"
          clearable
        />
        <p v-if="videoError" class="video-error">{{ videoError }}</p>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { createPost, updatePost, getPostDetail, syncUser, getCategoryList, uploadImage } from '@/api/forum'
import { isWxWorkEnv, getMockUser } from '@/utils/wxwork'
import { emojiList, emojiBasePath, getRecentEmojis, addRecentEmoji } from '@/config/emojis'

const router = useRouter()
const route = useRoute()

// 编辑模式判断
const isEditMode = computed(() => route.name === 'PostEdit')
const postId = computed(() => route.params.id)



// 表单数据
const title = ref('')
const content = ref('')
const imageList = ref([])
const submitting = ref(false)
const uploadProgress = ref(0)  // 上传进度百分比
const showEmojiPicker = ref(false)
const fileInput = ref(null)
const titleInputRef = ref(null)
const bottomOffset = ref(0)

// 分类数据
const categories = ref([])
const selectedCategory = ref(null)

// 视频相关
const videoUrl = ref('')
const showVideoDialog = ref(false)
const videoInputUrl = ref('')
const videoError = ref('')
const videoInputRef = ref(null)

// 支持的视频平台正则
const VIDEO_PLATFORMS = [
  { name: '优酷', pattern: /youku\.com|v\.youku\.com|m\.youku\.com/i },
  { name: '腾讯视频', pattern: /v\.qq\.com|m\.v\.qq\.com|qq\.com\/x\/cover/i },
  { name: 'B站', pattern: /bilibili\.com|b23\.tv/i }
]

// 是否可以提交
// 规则：1.分类必选 2.标题必填 3.内容（文字/图片/视频）至少一个
const canSubmit = computed(() => {
  // 1. 必须选择分类
  if (!selectedCategory.value) return false
  // 2. 必须有标题
  if (!title.value.trim()) return false
  // 3. 必须有内容（文字、图片、视频至少一种）
  const hasContent = content.value.trim().length > 0
  const hasImages = imageList.value.length > 0
  const hasVideo = !!videoUrl.value
  return hasContent || hasImages || hasVideo
})

// 草稿存储相关
const DRAFT_KEY = 'forum_post_draft'
let saveDraftTimer = null

// 保存草稿到 localStorage
function saveDraft() {
  const draft = {
    title: title.value,
    content: content.value,
    images: imageList.value,
    videoUrl: videoUrl.value,
    categoryId: selectedCategory.value,
    savedAt: Date.now()
  }
  localStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
}

// 节流保存草稿（2秒内只保存一次）
function saveDraftThrottled() {
  if (saveDraftTimer) clearTimeout(saveDraftTimer)
  saveDraftTimer = setTimeout(() => {
    saveDraft()
  }, 2000)
}

// 加载草稿
function loadDraft() {
  const draftStr = localStorage.getItem(DRAFT_KEY)
  if (!draftStr) return null
  try {
    return JSON.parse(draftStr)
  } catch {
    return null
  }
}

// 清除草稿
function clearDraft() {
  // 同时取消待执行的保存操作
  if (saveDraftTimer) {
    clearTimeout(saveDraftTimer)
    saveDraftTimer = null
  }
  localStorage.removeItem(DRAFT_KEY)
}

// 恢复草稿到表单
function restoreDraft(draft) {
  title.value = draft.title || ''
  content.value = draft.content || ''
  imageList.value = draft.images || []
  videoUrl.value = draft.videoUrl || ''
  selectedCategory.value = draft.categoryId || null
}

// 检查草稿是否有内容
function hasDraftContent(draft) {
  return draft && (draft.title || draft.content || (draft.images && draft.images.length > 0) || draft.videoUrl)
}

// iOS 输入法辅助栏高度（包含上下箭头和“完成”按钮）
const IOS_ACCESSORY_BAR_HEIGHT = 44

// 最近使用的表情
const recentEmojis = ref(getRecentEmojis())
const isWxWork = ref(true)
const isDev = ref(import.meta.env.DEV)

// 打开表情选择器时刷新最近使用列表
watch(showEmojiPicker, (newVal) => {
  if (newVal) {
    recentEmojis.value = getRecentEmojis()
  }
})

// 记录锁定的键盘高度和初始 offsetTop
let lockedKeyboardHeight = 0
let initialOffsetTop = 0

// 监听软键盘高度变化
function handleViewportResize() {
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

// 输入框获得焦点时
function onInputFocus() {
  showEmojiPicker.value = false
}

onMounted(async () => {
  // 禁止 body 滚动（仅在此页面）
  document.body.style.overflow = 'hidden'
  document.body.style.height = '100%'
  document.documentElement.style.overflow = 'hidden'
  document.documentElement.style.height = '100%'
  
  isWxWork.value = isWxWorkEnv()
  await initUser()
  
  // 加载分类列表
  try {
    const res = await getCategoryList()
    if (res.data) {
      categories.value = res.data
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
  
  // 编辑模式：加载帖子数据
  if (isEditMode.value && postId.value) {
    try {
      const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
      const res = await getPostDetail(postId.value, user.wxUserid)
      const post = res.data
      title.value = post.title || ''
      content.value = post.content || ''
      imageList.value = post.images ? JSON.parse(post.images) : []
      videoUrl.value = post.videoUrl || ''
      selectedCategory.value = post.categoryId || null
    } catch (e) {
      console.error('加载帖子失败', e)
      showToast('加载帖子失败')
    }
  } else {
    // 新建模式：检测草稿并询问是否恢复
    const draft = loadDraft()
    if (hasDraftContent(draft)) {
      try {
        await showDialog({
          title: '发现草稿',
          message: '检测到未完成的草稿，是否继续编辑？',
          showCancelButton: true,
          confirmButtonText: '恢复草稿',
          cancelButtonText: '放弃'
        })
        // 用户点击"恢复草稿"
        restoreDraft(draft)
      } catch {
        // 用户点击"放弃"
        clearDraft()
      }
    }
  }
  
  // 自动聚焦到标题输入框
  nextTick(() => {
    if (titleInputRef.value) {
      titleInputRef.value.focus()
    }
  })
  
  // 监听 visualViewport 变化（resize 和 scroll）
  if (window.visualViewport) {
    window.visualViewport.addEventListener('resize', handleViewportResize)
    window.visualViewport.addEventListener('scroll', handleViewportResize)
  }
})

onUnmounted(() => {
  // 恢复 html/body 滚动，使用 removeProperty 彻底清除
  document.body.style.removeProperty('overflow')
  document.body.style.removeProperty('height')
  document.documentElement.style.removeProperty('overflow')
  document.documentElement.style.removeProperty('height')
  
  if (window.visualViewport) {
    window.visualViewport.removeEventListener('resize', handleViewportResize)
    window.visualViewport.removeEventListener('scroll', handleViewportResize)
  }
})

// 监听表单变化，自动保存草稿（仅新建模式）
watch([title, content, imageList, videoUrl, selectedCategory], () => {
  // 编辑模式下不保存草稿
  if (!isEditMode.value) {
    saveDraftThrottled()
  }
}, { deep: true })

// 圆形进度条的 strokeDashoffset 计算
const progressOffset = computed(() => {
  const circumference = 2 * Math.PI * 45 // r=45
  return circumference - (uploadProgress.value / 100) * circumference
})

async function initUser() {
  const storedUser = localStorage.getItem('forumUser')
  if (storedUser) return

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



// 返回
function goBack() {
  if (title.value || content.value || imageList.value.length > 0 || videoUrl.value) {
    showDialog({
      title: '提示',
      message: '确定要放弃编辑吗？',
      showCancelButton: true
    }).then(() => {
      clearDraft() // 用户确认放弃编辑，清除草稿
      router.back()
    }).catch(() => {})
  } else {
    router.back()
  }
}

// 选择图片
function selectImage() {
  fileInput.value?.click()
}

// 视频链接验证
function validateVideoUrl(url) {
  if (!url || !url.trim()) {
    return '请输入视频链接'
  }
  // 检查是否是URL格式
  try {
    new URL(url)
  } catch {
    return '请输入有效的链接地址'
  }
  // 检查是否匹配支持的平台
  const matched = VIDEO_PLATFORMS.some(p => p.pattern.test(url))
  if (!matched) {
    return '仅支持优酷、腾讯视频、B站的视频链接'
  }
  return ''
}

// 获取视频平台名称
function getVideoPlatform(url) {
  for (const p of VIDEO_PLATFORMS) {
    if (p.pattern.test(url)) {
      return p.name
    }
  }
  return '视频'
}

// 截断URL显示
function truncateUrl(url) {
  if (!url) return ''
  return url.length > 30 ? url.substring(0, 30) + '...' : url
}

// 从分享文本中提取URL (如B站分享 "【标题-哔哩哔哩】 https://b23.tv/xxx")
function extractVideoUrl(text) {
  if (!text) return ''
  // 使用正则提取 http/https 开头的URL
  const urlMatch = text.match(/https?:\/\/[^\s]+/i)
  if (urlMatch) {
    return urlMatch[0]
  }
  return text.trim()
}

// 视频弹窗关闭前的回调
function onVideoDialogClose(action) {
  if (action === 'confirm') {
    // 先提取URL（处理B站等分享文本）
    const extractedUrl = extractVideoUrl(videoInputUrl.value)
    const error = validateVideoUrl(extractedUrl)
    if (error) {
      videoError.value = error
      return false // 阻止关闭
    }
    videoUrl.value = extractedUrl
    videoError.value = ''
    videoInputUrl.value = ''
    return true
  } else {
    // 取消
    videoError.value = ''
    videoInputUrl.value = ''
    return true
  }
}

// 视频弹窗完全打开后 focus 输入框
function onVideoDialogOpened() {
  // 延迟 focus，确保动画完成且键盘状态稳定
  setTimeout(() => {
    if (videoInputRef.value) {
      videoInputRef.value.focus()
    }
  }, 100)
}

// 移除视频
function removeVideo() {
  videoUrl.value = ''
}

// 压缩图片（优化后：maxWidth=800, quality=0.5 以加快上传）
function compressImage(file, maxWidth = 800, quality = 0.5) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        // 计算缩放后的尺寸
        let width = img.width
        let height = img.height
        
        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }
        
        // 使用 Canvas 进行压缩
        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)
        
        // 转换为 Base64
        const compressedDataUrl = canvas.toDataURL('image/jpeg', quality)
        resolve(compressedDataUrl)
      }
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}

// 处理图片选择（压缩后上传到服务器）
async function handleImageSelect(event) {
  const files = event.target.files
  if (!files || files.length === 0) return

  // 过滤非图片文件
  const imageFiles = Array.from(files).filter(file => file.type.startsWith('image/'))
  if (imageFiles.length === 0) {
    showToast('请选择图片文件')
    event.target.value = ''
    return
  }
  if (imageFiles.length < files.length) {
    showToast('已忽略非图片文件')
  }

  // 限制单次上传数量
  const MAX_SINGLE_UPLOAD = 20
  if (imageFiles.length > MAX_SINGLE_UPLOAD) {
    showToast(`单次最多选择${MAX_SINGLE_UPLOAD}张图片`)
    event.target.value = ''
    return
  }

  // 压缩阈值：500KB
  const COMPRESS_THRESHOLD = 500 * 1024

  // 逐张处理并上传
  for (const file of imageFiles) {
    try {
      let fileToUpload = file
      
      // 大于 500KB 的图片进行压缩
      if (file.size > COMPRESS_THRESHOLD) {
        fileToUpload = await compressImageToBlob(file)
      }
      
      // 上传到服务器
      const res = await uploadImage(fileToUpload)
      if (res.code === 200 && res.url) {
        // 存储服务器返回的 URL
        imageList.value.push(res.url)
      } else {
        showToast('图片上传失败')
      }
    } catch (e) {
      console.error('图片上传失败', e)
      showToast('图片上传失败: ' + (e.message || '未知错误'))
    }
  }

  // 清空input，允许重复选择同一文件
  event.target.value = ''
}

// 压缩图片并返回 Blob（用于上传）
function compressImageToBlob(file, maxWidth = 800, quality = 0.5) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        let width = img.width
        let height = img.height
        
        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }
        
        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)
        
        // 转换为 Blob
        canvas.toBlob((blob) => {
          // 创建一个新的 File 对象，保持原文件名
          const compressedFile = new File([blob], file.name, { type: 'image/jpeg' })
          resolve(compressedFile)
        }, 'image/jpeg', quality)
      }
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}

// 移除图片
function removeImage(index) {
  imageList.value.splice(index, 1)
}

// 切换表情面板
function toggleEmojiPicker() {
  showEmojiPicker.value = !showEmojiPicker.value
  if (showEmojiPicker.value) {
    recentEmojis.value = getRecentEmojis()
  }
}

// 插入表情（插入 [表情名] 格式，保持面板打开）
function insertEmoji(emoji) {
  content.value += `[${emoji.name}]`
  // 保存到最近使用 (localStorage)，但不刷新UI列表
  addRecentEmoji(emoji)
  // 不关闭面板，方便连续选择
}

// 删除表情或字符（智能删除）
function deleteEmojiOrChar() {
  if (!content.value) return
  
  // 检查是否以 [表情名] 结尾
  const emojiTagMatch = content.value.match(/\[[^\]]+\]$/)
  if (emojiTagMatch) {
    content.value = content.value.slice(0, -emojiTagMatch[0].length)
  } else {
    content.value = content.value.slice(0, -1)
  }
}

// 提交帖子
async function submitPost() {
  if (!canSubmit.value || submitting.value) return

  const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
  if (!user.wxUserid) {
    showToast('用户信息获取失败，请刷新页面')
    return
  }

  submitting.value = true
  
  try {
    if (isEditMode.value) {
      // 编辑模式：调用更新接口
      await updatePost({
        postId: postId.value,
        wxUserid: user.wxUserid,
        title: title.value.trim(),
        content: content.value.trim(),
        images: imageList.value.length > 0 ? JSON.stringify(imageList.value) : '',
        videoUrl: videoUrl.value || '',
        categoryId: selectedCategory.value
      })
      showToast('修改成功')
      clearDraft() // 修改成功后也清除草稿
    } else {
      // 新建模式：调用创建接口（图片已提前上传，这里只传 URL）
      await createPost({
        wxUserid: user.wxUserid,
        title: title.value.trim(),
        content: content.value.trim(),
        images: imageList.value.length > 0 ? JSON.stringify(imageList.value) : '',
        videoUrl: videoUrl.value || '',
        userUnit: user.unit || '',
        userDept: user.department || '',
        categoryId: selectedCategory.value
      })
      showToast('发布成功')
      clearDraft() // 发布成功后清除草稿
    }
    
    // 等待一下让用户看到成功提示
    setTimeout(() => {
      if (isEditMode.value) {
        router.replace(`/post/${postId.value}`)
      } else {
        router.replace('/posts')
      }
    }, 500)
    
  } catch (e) {
    console.error(e)
    // 获取详细错误信息
    let errorMsg = isEditMode.value ? '修改失败' : '发布失败'
    if (e.response?.data?.msg) {
      errorMsg = e.response.data.msg
    } else if (e.message) {
      errorMsg = e.message
    }
    showToast(errorMsg)
  } finally {
    submitting.value = false
    uploadProgress.value = 0
  }
}
</script>

<style scoped>
.post-create-page {
  position: fixed; /* 强制固定在视口内 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  display: flex;
  flex-direction: column;
  padding-top: 20px;
  width: 100%;
  overflow: hidden; /* 禁止整体滚动 */
  box-sizing: border-box;
  z-index: 1; /* 确保在最底层 */
}

.form-content {
  flex: 1;
  padding: 0 16px;
  overflow-y: auto; /* 只允许内容区域滚动 */
  overflow-x: hidden;
  padding-bottom: 70px;
  box-sizing: border-box;
  -webkit-overflow-scrolling: touch; /* iOS 惯性滚动 */
}

@media (prefers-color-scheme: dark) {
  .post-create-page {
    background: #000;
  }
  .form-content {
    background: #000;
  }
  /* 覆盖 Vant 默认白色背景 */
  .form-content :deep(.van-cell) {
    background: #000;
  }
  .form-content :deep(.van-field) {
    background: #000;
  }
}

/* 标题输入框样式 */
.title-input {
  padding: 10px 0;
  background: transparent;
}

.title-input :deep(.van-field__control) {
  font-size: 22px;
  font-weight: 500;
  color: #333;
  caret-color: #1989fa;
  background: transparent;
}

@media (prefers-color-scheme: dark) {
  .title-input {
    background: #000;
  }
  .title-input :deep(.van-cell) {
    background: #000;
  }
  .title-input :deep(.van-field__control) {
    color: #f5f5f5;
    background: transparent;
  }
}

.title-input :deep(.van-field__control::placeholder) {
  color: #c9c9c9;
  font-weight: 500;
}

/* 内容输入框样式 */
.content-input {
  padding: 10px 0;
  background: transparent;
}

.content-input :deep(.van-field__control) {
  font-size: 17px;
  line-height: 1.6;
  color: #333;
  background: transparent;
}

@media (prefers-color-scheme: dark) {
  .content-input {
    background: #000;
  }
  .content-input :deep(.van-cell) {
    background: #000;
  }
  .content-input :deep(.van-field__control) {
    color: #f5f5f5;
    background: transparent;
  }
}

.content-input :deep(.van-field__control::placeholder) {
  color: #c9c9c9;
}

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.preview-item {
  position: relative;
}

.remove-btn {
  position: absolute;
  top: -6px;
  right: -6px;
  font-size: 18px;
  color: #ee0a24;
  background: #fff;
  border-radius: 50%;
}

/* 分类选择器 */
.category-picker {
  padding: 12px 0 8px;
  background: #fff;
}

.category-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.category-label {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.required-mark {
  color: #ee0a24;
  margin-left: 2px;
  font-size: 14px;
}

.category-hint {
  font-size: 12px;
  color: #ee0a24;
  margin-left: 10px;
}

.category-scroll {
  display: flex;
  overflow-x: auto;
  gap: 10px;
  -webkit-overflow-scrolling: touch;
}

.category-scroll::-webkit-scrollbar {
  display: none;
}

.category-tag {
  flex-shrink: 0;
  padding: 5px 12px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  border-radius: 14px;
  border: 1px solid transparent;
  white-space: nowrap;
}

.category-tag.active {
  color: #fff;
  background: #1989fa;
  border-color: #1989fa;
}

@media (prefers-color-scheme: dark) {
  .category-picker {
    background: #000;
  }
  .category-label {
    color: #f5f5f5;
  }
  .category-hint {
    color: #ff6b6b;
  }
  .category-tag {
    color: #aaa;
    background: #2c2c2c;
  }
  .category-tag.active {
    color: #fff;
    background: #1989fa;
    border-color: #1989fa;
  }
}

/* 底部工具栏容器 */
.bottom-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: var(--bg-color, #fff);
}

/* 底部工具栏 */
.toolbar {
  height: 50px;
  background: #f7f7f7; /* 浅灰底色 */
  border-top: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  padding: 0 16px;
  /* 移除 position: fixed */
}

@media (prefers-color-scheme: dark) {
  .bottom-container {
     background: #191919;
  }
  .toolbar {
    background: #2c2c2c;
    border-top: 1px solid #333;
  }
}

.left-tools {
  display: flex;
  align-items: center;
  gap: 24px; /* 图标间距 */
  flex: 1;
}

.tool-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}

.tool-icon .van-icon {
  font-size: 24px;
  font-weight: normal; 
}

/* 关闭按钮稍微细一点，可以用 thinner icon 或者 scale */
.close-btn .van-icon {
  color: #999;
}

.tool-icon:active {
  opacity: 0.6;
}

.send-btn {
  width: 32px;
  height: 32px;
  background: #e1e9f5; /* 禁用/空状态的浅蓝色 */
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.send-btn .van-icon {
  font-size: 18px;
  font-weight: bold;
}

/* 激活状态（有内容可发送） */
.send-btn.active {
  background: #1989fa; /* 亮蓝色 */
}

/* 禁用状态保持浅色 */
.send-btn.disabled {
  background: #e1e9f5;
}

@media (prefers-color-scheme: dark) {
  .tool-icon {
    color: #aaa;
  }
  .send-btn {
    background: #333;
    color: #555;
  }
  .send-btn.active {
    background: #1989fa;
    color: #fff;
  }
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 表情选择面板 (内联式) */
.emoji-picker-panel {
  background: #1a1a1a;
  border-top: 1px solid #333;
  position: relative;
}

.emoji-picker-content {
  padding: 12px;
  max-height: 280px;
  overflow-y: auto;
}

.emoji-section {
  margin-bottom: 12px;
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
  gap: 6px;
}

.emoji-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  cursor: pointer;
  border-radius: 6px;
}

.emoji-item img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.emoji-item:active {
  background: #333;
}

/* 删除按钮区域 */
.emoji-actions {
  position: absolute;
  bottom: 16px;
  right: 16px;
}

.action-btn {
  width: 44px;
  height: 36px;
  background: #333; /* 深色主题默认 */
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #f5f5f5; /* 浅色图标 */
}

.action-btn .van-icon {
  font-size: 20px;
  color: inherit;
}

.action-btn:active {
  opacity: 0.7;
}

@media (prefers-color-scheme: light) {
  .emoji-picker-panel {
    background: #fff;
    border-top: 1px solid #eee;
  }
  .emoji-item:active {
    background: #f0f0f0;
  }
  .action-btn {
    background: #f5f5f5;
    color: #333;
  }
}

/* 视频预览 */
.video-preview {
  margin-top: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
}

.video-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.video-platform {
  background: #1989fa;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.video-url {
  flex: 1;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-video {
  color: #ee0a24;
  font-size: 18px;
  cursor: pointer;
}

@media (prefers-color-scheme: dark) {
  .video-preview {
    background: #2c2c2c;
  }
  .video-info {
    color: #aaa;
  }
  .video-url {
    color: #666;
  }
}

/* 视频弹窗 */
.video-dialog-content {
  padding: 16px;
}

.video-tips {
  font-size: 13px;
  color: #999;
  margin: 0 0 12px 0;
  text-align: center;
}

.video-error {
  font-size: 12px;
  color: #ee0a24;
  margin: 8px 0 0 0;
}

/* 上传进度遮罩 */
.upload-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.upload-progress-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.progress-circle {
  position: relative;
  width: 100px;
  height: 100px;
}

.progress-circle svg {
  transform: rotate(-90deg);
  width: 100%;
  height: 100%;
}

.progress-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.2);
  stroke-width: 8;
}

.progress-bar {
  fill: none;
  stroke: #1989fa;
  stroke-width: 8;
  stroke-linecap: round;
  stroke-dasharray: 282.74; /* 2 * PI * 45 */
  transition: stroke-dashoffset 0.1s ease;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}

.upload-hint {
  margin-top: 16px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}
</style>
