<template>
  <div class="post-item" :class="{ 'is-new-post': isNew }" @click="$emit('click')">
    <!-- 标题 -->
    <div class="post-title">
      <span v-if="isPinned" class="pin-tag">【置顶】</span>
      {{ post.title }}
    </div>
    
    <!-- 内容摘要 (支持表情渲染) -->
    <div class="post-content" v-html="truncatedContent"></div>
    
    <!-- 如果没有文字内容但有图片，显示图片图标 -->
    <div v-if="showImageIcon" class="post-image-icon">
      <van-icon name="photo-o" size="20" />
    </div>
    
    <!-- 如果没有文字内容但有视频，显示视频图标 -->
    <div v-if="showVideoIcon" class="post-video-icon">
      <van-icon name="video-o" size="20" />
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
</template>

<script setup>
import { computed } from 'vue'
import { renderEmojis } from '@/config/emojis'

const props = defineProps({
  post: {
    type: Object,
    required: true
  },
  isNew: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click'])

// 判断是否置顶中
const isPinned = computed(() => {
  if (props.post.isPinned !== '1') return false
  if (!props.post.pinExpireTime) return true // 永久置顶
  return new Date(props.post.pinExpireTime) > new Date()
})

// 截断内容 (并渲染表情)
const truncatedContent = computed(() => {
  const content = props.post.content
  if (!content) return ''
  const truncated = content.length > 50 ? content.substring(0, 50) + '...' : content
  return renderEmojis(truncated)
})

// 是否显示图片图标
const showImageIcon = computed(() => {
  const content = props.post.content
  return (!content || !content.trim()) && props.post.images
})

// 是否显示视频图标
const showVideoIcon = computed(() => {
  const content = props.post.content
  return (!content || !content.trim()) && !props.post.images && props.post.videoUrl
})
</script>

<style scoped>
.post-item {
  position: relative;
  background: #fff;
  padding: 16px 16px 12px 16px;
  margin-bottom: 10px;
  border-bottom: none;
}

/* 新帖蓝条标记 */
.post-item.is-new-post::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 4px;
  background: #1989fa;
  border-radius: 0 2px 2px 0;
  z-index: 1;
}

@media (prefers-color-scheme: dark) {
  .post-item {
    background: #191919;
    margin-bottom: 10px;
    border-bottom: none;
  }
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #000;
  line-height: 1.4;
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
  font-size: 13px;
  color: #999;
}

.author-name {
  color: #999;
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
</style>
