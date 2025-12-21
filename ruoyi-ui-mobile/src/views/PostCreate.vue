<template>
  <div class="post-create-page">
    <!-- 表单内容 -->
    <div class="form-content">
      <van-field
        v-model="title"
        placeholder="主题"
        :border="false"
        class="title-input"
        maxlength="50"
        @focus="showEmojiPicker = false"
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
        @focus="showEmojiPicker = false"
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
    </div>

    <!-- 底部功能区固定容器 -->
    <div class="bottom-container">
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { createPost, syncUser } from '@/api/forum'
import { isWxWorkEnv, getMockUser } from '@/utils/wxwork'
import { emojiList, emojiBasePath, getRecentEmojis, addRecentEmoji } from '@/config/emojis'

const router = useRouter()

// 表单数据
const title = ref('')
const content = ref('')
const imageList = ref([])
const submitting = ref(false)
const showEmojiPicker = ref(false)
const fileInput = ref(null)
const keyboardHeight = ref(0) // 实际开发中难以精确获取软键盘高度，通常依赖布局自适应

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

onMounted(async () => {
  isWxWork.value = isWxWorkEnv()
  // 初始化用户
  await initUser()
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

// 是否可以提交
const canSubmit = computed(() => {
  return title.value.trim().length > 0 && content.value.trim().length > 0
})

// 返回
function goBack() {
  if (title.value || content.value || imageList.value.length > 0) {
    showDialog({
      title: '提示',
      message: '确定要放弃编辑吗？',
      showCancelButton: true
    }).then(() => {
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

// 处理图片选择
function handleImageSelect(event) {
  const files = event.target.files
  if (!files || files.length === 0) return

  // 限制最多9张图片
  if (imageList.value.length + files.length > 9) {
    showToast('最多只能上传9张图片')
    return
  }

  Array.from(files).forEach(file => {
    const reader = new FileReader()
    reader.onload = (e) => {
      imageList.value.push(e.target.result)
    }
    reader.readAsDataURL(file)
  })

  // 清空input，允许重复选择同一文件
  event.target.value = ''
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
    const res = await createPost({
      wxUserid: user.wxUserid,
      title: title.value.trim(),
      content: content.value.trim(),
      images: imageList.value.length > 0 ? JSON.stringify(imageList.value) : ''
    })
    
    // 如果返回成功
    showToast('发布成功')
    
    // 等待一下让用户看到成功提示
    setTimeout(() => {
      router.replace('/posts')
    }, 500)
    
  } catch (e) {
    console.error(e)
    showToast('发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.post-create-page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  padding-top: 20px; /* 顶部留白，模拟状态栏下方 */
}

.form-content {
  flex: 1;
  padding: 0 16px;
  overflow-y: auto;
  padding-bottom: 70px; /* 防止内容被底部遮挡 */
}

@media (prefers-color-scheme: dark) {
  .post-create-page {
    background: #191919;
  }
}

/* 标题输入框样式 */
.title-input {
  padding: 10px 0;
}

.title-input :deep(.van-field__control) {
  font-size: 22px; /* 大字号 */
  font-weight: 500;
  color: #333;
  /* 调整光标颜色为蓝色 */
  caret-color: #1989fa;
}

@media (prefers-color-scheme: dark) {
  .title-input :deep(.van-field__control) {
    color: #f5f5f5;
  }
}

.title-input :deep(.van-field__control::placeholder) {
  color: #c9c9c9; /* 浅灰色 placeholder */
  font-weight: 500;
}

/* 内容输入框样式 */
.content-input {
  padding: 10px 0;
}

.content-input :deep(.van-field__control) {
  font-size: 17px;
  line-height: 1.6;
  color: #333;
}

@media (prefers-color-scheme: dark) {
  .content-input :deep(.van-field__control) {
    color: #f5f5f5;
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

/* 底部工具栏容器 */
.bottom-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: var(--bg-color, #fff); /* 避免透视背景 */
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
  background: #f5f5f5;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.action-btn .van-icon {
  font-size: 20px;
  color: #333;
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
}
</style>
