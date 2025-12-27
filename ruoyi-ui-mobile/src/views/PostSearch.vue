<template>
  <div class="post-search-page">
    <!-- 顶部搜索栏 -->
    <div class="search-header">
      <van-search
        v-model="keyword"
        placeholder="搜索帖子标题"
        show-action
        action-text="搜索"
        @search="doSearch"
        @click-right-icon="doSearch"
      >
        <template #action>
          <div @click="doSearch">搜索</div>
        </template>
      </van-search>
      <div class="clear-btn" @click="clearSearch">清空</div>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <div class="filter-scroll">
        <span 
          class="filter-tag" 
          :class="{ active: selectedCategoryId === null }"
          @click="selectCategory(null)"
        >全部</span>
        <span 
          v-for="cat in categories" 
          :key="cat.categoryId"
          class="filter-tag"
          :class="{ active: selectedCategoryId === cat.categoryId }"
          @click="selectCategory(cat.categoryId)"
        >{{ cat.name }}</span>
      </div>
    </div>

    <!-- 搜索历史（未搜索时显示） -->
    <div v-if="!hasSearched && searchHistory.length > 0" class="search-history">
      <div class="history-header">
        <span>历史搜索</span>
        <van-icon name="delete-o" @click="clearHistory" />
      </div>
      <div class="history-tags">
        <span 
          v-for="(item, index) in searchHistory" 
          :key="index" 
          class="history-tag"
          @click="searchFromHistory(item)"
        >{{ item }}</span>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="hasSearched" class="search-results">
      <!-- 骨架屏 -->
      <div v-if="isLoading" class="skeleton-container">
        <div v-for="i in 3" :key="i" class="skeleton-card">
          <van-skeleton title :row="2" />
        </div>
      </div>

      <!-- 结果列表 -->
      <template v-else>
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="loadMore"
        >
          <PostItem
            v-for="post in resultList"
            :key="post.postId"
            :post="post"
            @click="goToDetail(post.postId)"
          />
        </van-list>

        <!-- 无结果 -->
        <van-empty v-if="resultList.length === 0 && !loading" description="未找到相关帖子" />
      </template>
    </div>

    <!-- 未搜索且无历史时的提示 -->
    <div v-if="!hasSearched && searchHistory.length === 0" class="search-hint">
      <van-icon name="search" size="60" color="#ddd" />
      <p>输入关键词搜索帖子</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList, getCategoryList } from '@/api/forum'
import PostItem from '@/components/PostItem.vue'

const router = useRouter()

// 搜索相关
const keyword = ref('')
const hasSearched = ref(false)
const isLoading = ref(false)

// 分类
const categories = ref([])
const selectedCategoryId = ref(null)

// 结果列表
const resultList = ref([])
const loading = ref(false)
const finished = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)

// 搜索历史
const HISTORY_KEY = 'post_search_history'
const MAX_HISTORY = 10
const searchHistory = ref([])

onMounted(async () => {
  // 加载分类
  try {
    const res = await getCategoryList()
    categories.value = res.rows || res.data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
  
  // 加载历史记录
  loadHistory()
})

// 加载搜索历史
function loadHistory() {
  try {
    const data = localStorage.getItem(HISTORY_KEY)
    searchHistory.value = data ? JSON.parse(data) : []
  } catch {
    searchHistory.value = []
  }
}

// 保存搜索历史
function saveHistory(kw) {
  if (!kw.trim()) return
  // 去重
  const filtered = searchHistory.value.filter(item => item !== kw)
  // 添加到开头
  filtered.unshift(kw)
  // 限制数量
  searchHistory.value = filtered.slice(0, MAX_HISTORY)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(searchHistory.value))
}

// 清除历史
function clearHistory() {
  searchHistory.value = []
  localStorage.removeItem(HISTORY_KEY)
}

// 从历史记录搜索
function searchFromHistory(kw) {
  keyword.value = kw
  doSearch()
}

// 选择分类
function selectCategory(categoryId) {
  selectedCategoryId.value = categoryId
  // 如果已经搜索过，切换分类后重新搜索
  if (hasSearched.value && keyword.value.trim()) {
    resetAndSearch()
  }
}

// 执行搜索
function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  
  // 保存历史
  saveHistory(kw)
  
  // 重置并搜索
  resetAndSearch()
}

// 重置并搜索
function resetAndSearch() {
  hasSearched.value = true
  resultList.value = []
  pageNum.value = 1
  finished.value = false
  isLoading.value = true
  
  fetchResults()
}

// 加载更多
function loadMore() {
  if (!hasSearched.value) {
    finished.value = true
    return
  }
  fetchResults()
}

// 获取搜索结果
async function fetchResults() {
  try {
    const user = JSON.parse(localStorage.getItem('forumUser') || '{}')
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      title: keyword.value.trim()
    }
    
    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }
    
    // 传入 wxUserid 用于限流过滤
    if (user.wxUserid) {
      params.wxUserid = user.wxUserid
    }
    
    const res = await getPostList(params)
    const rows = res.rows || []
    
    if (pageNum.value === 1) {
      resultList.value = rows
    } else {
      resultList.value = [...resultList.value, ...rows]
    }
    
    if (rows.length < pageSize.value) {
      finished.value = true
    } else {
      pageNum.value++
    }
  } catch (e) {
    console.error('搜索失败', e)
    finished.value = true
  } finally {
    loading.value = false
    isLoading.value = false
  }
}

// 跳转详情
function goToDetail(postId) {
  router.push(`/post/${postId}`)
}

// 清空搜索（清空输入框、结果列表，显示历史记录）
function clearSearch() {
  keyword.value = ''
  hasSearched.value = false
  resultList.value = []
  pageNum.value = 1
  finished.value = false
}

// 返回
function goBack() {
  router.back()
}
</script>

<style scoped>
.post-search-page {
  min-height: 100vh;
  background: #f5f5f5;
  /* 企业微信导航栏已经在外层儆入，不需要额外 padding */
}

@media (prefers-color-scheme: dark) {
  .post-search-page {
    background: #000;
  }
}

.search-header {
  display: flex;
  align-items: center;
  background: #fff;
  padding-right: 12px;
}

.search-header :deep(.van-search) {
  flex: 1;
}

.clear-btn {
  color: #1989fa;
  font-size: 14px;
  padding: 0 4px;
}

@media (prefers-color-scheme: dark) {
  .search-header {
    background: #191919;
  }
  .search-header :deep(.van-search) {
    background: #191919;
  }
  .search-header :deep(.van-search__content) {
    background: #2c2c2c;
  }
  .search-header :deep(.van-field__control) {
    color: #f5f5f5;
  }
  /* 搜索按钮深色模式 */
  .search-header :deep(.van-search__action) {
    color: #4da3ff;
  }
  .clear-btn {
    color: #4da3ff;
  }
}

/* 分类筛选 */
.category-filter {
  background: #fff;
  border-bottom: 1px solid #eee;
}

@media (prefers-color-scheme: dark) {
  .category-filter {
    background: #191919;
    border-bottom-color: #333;
  }
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
  .filter-tag {
    color: #aaa;
    background: #2c2c2c;
  }
  .filter-tag.active {
    color: #fff;
    background: #1989fa;
  }
}

/* 搜索历史 */
.search-history {
  padding: 16px;
  background: #fff;
  margin-bottom: 10px;
}

@media (prefers-color-scheme: dark) {
  .search-history {
    background: #191919;
  }
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

@media (prefers-color-scheme: dark) {
  .history-header {
    color: #999;
  }
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.history-tag {
  padding: 6px 14px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  border-radius: 14px;
}

@media (prefers-color-scheme: dark) {
  .history-tag {
    color: #aaa;
    background: #2c2c2c;
  }
}

/* 搜索结果 */
.search-results {
  padding-bottom: 20px;
}

/* 骨架屏 */
.skeleton-container {
  padding: 0 16px;
}

.skeleton-card {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
}

@media (prefers-color-scheme: dark) {
  .skeleton-card {
    background: #191919;
  }
}

/* 搜索提示 */
.search-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100px;
  color: #999;
}

.search-hint p {
  margin-top: 16px;
  font-size: 14px;
}
</style>
