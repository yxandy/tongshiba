<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入帖子标题" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="作者" prop="nickname">
        <el-input v-model="queryParams['user.nickname']" placeholder="请输入作者昵称" clearable style="width: 150px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="单位" prop="userUnit">
        <el-input v-model="queryParams.userUnit" placeholder="请输入发帖时单位" clearable style="width: 150px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="部门" prop="userDept">
        <el-input v-model="queryParams.userDept" placeholder="请输入发帖时部门" clearable style="width: 150px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="锁定状态" prop="isLocked">
        <el-select v-model="queryParams.isLocked" placeholder="锁定状态" clearable style="width: 100px">
          <el-option label="正常" value="0" />
          <el-option label="已锁定" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="删除状态" prop="delFlag">
        <el-select v-model="queryParams.delFlag" placeholder="删除状态" style="width: 100px">
          <el-option label="正常" value="0" />
          <el-option label="已删除" value="1" />
          <el-option label="全部" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="置顶状态" prop="isPinned">
        <el-select v-model="queryParams.isPinned" placeholder="置顶状态" clearable style="width: 100px">
          <el-option label="置顶中" value="1" />
          <el-option label="未置顶" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['forum:post:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="postList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="postId" width="80" />
      <el-table-column label="标题" prop="title" :show-overflow-tooltip="true" min-width="200">
        <template slot-scope="scope">
          <el-link type="primary" @click="handleView(scope.row)">{{ scope.row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="作者" width="120">
        <template slot-scope="scope">{{ scope.row.user ? scope.row.user.nickname : '-' }}</template>
      </el-table-column>
      <el-table-column label="发帖时单位" prop="userUnit" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="发帖时部门" prop="userDept" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="浏览" prop="viewCount" width="70" align="center" />
      <el-table-column label="评论" prop="commentCount" width="70" align="center" />
      <el-table-column label="锁定" prop="isLocked" width="70" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isLocked === '1' ? 'danger' : 'success'" size="small">
            {{ scope.row.isLocked === '1' ? '已锁定' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="删除" prop="delFlag" width="70" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.delFlag === '1' ? 'info' : ''" size="small">
            {{ scope.row.delFlag === '1' ? '已删除' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="130" align="center">
        <template slot-scope="scope">
          <template v-if="isPinned(scope.row)">
            <el-tag type="warning" size="small">{{ getPinStatus(scope.row) }}</el-tag>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="300" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleView(scope.row)">查看</el-button>
          <el-button size="mini" type="text" @click="handleViewLog(scope.row)">日志</el-button>
          <el-button size="mini" type="text" v-if="!isPinned(scope.row) && scope.row.delFlag !== '1'" @click="handlePin(scope.row)" v-hasPermi="['forum:post:lock']">置顶</el-button>
          <el-button size="mini" type="text" v-if="isPinned(scope.row) && scope.row.delFlag !== '1'" style="color: #e6a23c" @click="handleUnpin(scope.row)" v-hasPermi="['forum:post:lock']">取消置顶</el-button>
          <el-button size="mini" type="text" v-if="scope.row.isLocked === '0' && scope.row.delFlag !== '1'" @click="handleLock(scope.row)" v-hasPermi="['forum:post:lock']">锁定</el-button>
          <el-button size="mini" type="text" v-if="scope.row.isLocked === '1' && scope.row.delFlag !== '1'" @click="handleUnlock(scope.row)" v-hasPermi="['forum:post:lock']">解锁</el-button>
          <el-button size="mini" type="text" v-if="scope.row.delFlag !== '1'" style="color: #f56c6c" @click="handleDelete(scope.row)" v-hasPermi="['forum:post:remove']">删除</el-button>
          <el-button size="mini" type="text" v-if="scope.row.delFlag === '1'" style="color: #67c23a" @click="handleRestore(scope.row)" v-hasPermi="['forum:post:remove']">恢复</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 详情弹窗 -->
    <el-dialog :title="detailTitle" :visible.sync="detailOpen" width="800px" append-to-body>
      <div v-if="detailData" class="post-detail">
        <h2>{{ detailData.title }}</h2>
        <div class="post-meta">
          <span>作者：{{ detailData.user ? detailData.user.nickname : '匿名' }}</span>
          <span>单位：{{ detailData.userUnit || '-' }}</span>
          <span>部门：{{ detailData.userDept || '-' }}</span>
          <span>发布时间：{{ detailData.createTime }}</span>
          <span>浏览：{{ detailData.viewCount }}</span>
          <span>评论：{{ detailData.commentCount }}</span>
        </div>
        <el-divider />
        <div class="post-content">{{ detailData.content }}</div>
        
        <!-- 图片展示 -->
        <div v-if="detailImages.length > 0" class="post-images">
          <el-image v-for="(img, idx) in detailImages" :key="idx" :src="img" :preview-src-list="detailImages" fit="cover" style="width: 150px; height: 150px; margin: 5px;" />
        </div>

        <!-- 视频展示 -->
        <div v-if="detailData.videoUrl" class="post-video">
          <div v-if="getEmbedUrl(detailData.videoUrl)" class="video-embed-wrapper">
            <iframe :src="getEmbedUrl(detailData.videoUrl)" frameborder="0" allowfullscreen class="video-iframe"></iframe>
          </div>
          <div v-else class="video-link">
            <i class="el-icon-video-camera"></i>
            <a :href="detailData.videoUrl" target="_blank">{{ detailData.videoUrl }}</a>
          </div>
        </div>

        <el-divider />
        <!-- 评论管理区域 -->
        <div class="comment-section">
          <div class="comment-header">
            <div style="display: flex; align-items: center; gap: 10px;">
              <h4 style="margin: 0;">评论列表（共 {{ detailData.commentCount }} 条）</h4>
              <el-button size="mini" type="text" @click="handleViewCommentLog">删除日志</el-button>
            </div>
            <el-select v-model="commentDelFlag" placeholder="删除状态" size="mini" style="width: 100px" @change="loadComments">
              <el-option label="正常" value="0" />
              <el-option label="已删除" value="1" />
              <el-option label="全部" value="2" />
            </el-select>
          </div>
          <el-table v-loading="commentLoading" :data="commentList" size="mini" max-height="300">
            <el-table-column label="楼层" prop="floorNum" width="60" align="center">
              <template slot-scope="scope">{{ scope.row.floorNum }}#</template>
            </el-table-column>
            <el-table-column label="评论者" width="80">
              <template slot-scope="scope">{{ scope.row.user ? scope.row.user.nickname : '-' }}</template>
            </el-table-column>
            <el-table-column label="评论时单位" prop="userUnit" width="120" :show-overflow-tooltip="true" />
            <el-table-column label="评论时部门" prop="userDept" width="100" :show-overflow-tooltip="true" />
            <el-table-column label="内容" prop="content" :show-overflow-tooltip="true" />
            <el-table-column label="时间" prop="createTime" width="140" />
            <el-table-column label="状态" width="70" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.delFlag === '1' ? 'info' : ''" size="mini">
                  {{ scope.row.delFlag === '1' ? '已删除' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="scope">
                <el-button v-if="scope.row.delFlag !== '1'" size="mini" type="text" style="color: #f56c6c" @click="handleDeleteComment(scope.row)" v-hasPermi="['forum:comment:remove']">删除</el-button>
                <el-button v-else size="mini" type="text" style="color: #67c23a" @click="handleRestoreComment(scope.row)" v-hasPermi="['forum:comment:remove']">恢复</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="commentList.length === 0 && !commentLoading" description="暂无评论" />
        </div>
      </div>
    </el-dialog>

    <!-- 置顶时长选择弹窗 -->
    <el-dialog title="设置置顶时长" :visible.sync="pinDialogVisible" width="500px" append-to-body custom-class="pin-dialog">
      <div class="pin-dialog-content">
        <p class="pin-hint">请选择帖子置顶的时长，过期后自动取消置顶：</p>
        <el-radio-group v-model="pinHours" size="medium" class="pin-options">
          <el-radio v-for="opt in pinOptions" :key="opt.value" :label="opt.value" border style="margin-right: 10px; margin-bottom: 15px; width: 100px">{{ opt.label }}</el-radio>
        </el-radio-group>
      </div>
      <div slot="footer">
        <el-button @click="pinDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPin">确定置顶</el-button>
      </div>
    </el-dialog>

    <!-- 操作日志弹窗 -->
    <el-dialog title="帖子操作日志" :visible.sync="logDialogVisible" width="600px" append-to-body>
      <el-table v-loading="logLoading" :data="logList" size="small" max-height="400">
        <el-table-column label="时间" prop="operateTime" width="160" />
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">{{ getActionLabel(scope.row.action) }}</template>
        </el-table-column>
        <el-table-column label="操作人" prop="operatorName" width="100" />
        <el-table-column label="详情" prop="description" :show-overflow-tooltip="true" />
      </el-table>
      <el-empty v-if="logList.length === 0 && !logLoading" description="暂无操作日志" />
    </el-dialog>

    <!-- 评论删除日志弹窗 -->
    <el-dialog title="评论删除日志" :visible.sync="commentLogDialogVisible" width="700px" append-to-body>
      <el-table v-loading="commentLogLoading" :data="commentLogList" size="small" max-height="400">
        <el-table-column label="时间" prop="operateTime" width="160" />
        <el-table-column label="楼层" width="60" align="center">
          <template slot-scope="scope">{{ scope.row.floorNum }}#</template>
        </el-table-column>
        <el-table-column label="评论内容" prop="contentSummary" :show-overflow-tooltip="true" />
        <el-table-column label="删除人" prop="operatorName" width="100" />
      </el-table>
      <el-empty v-if="commentLogList.length === 0 && !commentLogLoading" description="暂无删除日志" />
    </el-dialog>
  </div>
</template>

<script>
import { listPost, getPost, delPost, lockPost, unlockPost, restorePost, pinPost, unpinPost, getPostLog } from '@/api/forum/post'
import { listCommentByPost, delComment, restoreComment, getCommentLog } from '@/api/forum/comment'

export default {
  name: 'ForumPost',
  data() {
    return {
      loading: false,
      showSearch: true,
      postList: [],
      total: 0,
      ids: [],
      multiple: true,
      detailOpen: false,
      detailTitle: '',
      detailData: null,
      pinDialogVisible: false,
      pinPostId: null,
      pinHours: 0,
      logDialogVisible: false,
      logLoading: false,
      logList: [],
      pinOptions: [
        { label: '1小时', value: 1 },
        { label: '6小时', value: 6 },
        { label: '12小时', value: 12 },
        { label: '1天', value: 24 },
        { label: '3天', value: 72 },
        { label: '7天', value: 168 },
        { label: '永久', value: 0 }
      ],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        'user.nickname': undefined,
        userUnit: undefined,
        userDept: undefined,
        isLocked: undefined,
        delFlag: '0',
        isPinned: undefined
      },
      commentList: [],
      commentLoading: false,
      commentDelFlag: '0',
      commentLogDialogVisible: false,
      commentLogLoading: false,
      commentLogList: []
    }
  },
  computed: {
    detailImages() {
      if (!this.detailData || !this.detailData.images) return []
      try {
        return JSON.parse(this.detailData.images)
      } catch (e) {
        return []
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getEmbedUrl(url) {
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
      // 腾讯视频
      const qqMatch = url.match(/v\.qq\.com\/x\/(?:cover\/[\w]+\/|page\/)([\w]+)\.html/i)
      if (qqMatch) {
        return `https://v.qq.com/txp/iframe/player.html?vid=${qqMatch[1]}`
      }
      // 优酷
      const youkuMatch = url.match(/v\.youku\.com\/v_show\/id_([\w=]+)/i)
      if (youkuMatch) {
        return `https://player.youku.com/embed/${youkuMatch[1]}`
      }
      return null
    },
    isPinned(row) {
      if (row.isPinned !== '1') return false
      if (!row.pinExpireTime) return true // 永久置顶
      return new Date(row.pinExpireTime) > new Date()
    },
    getPinStatus(row) {
      if (!row.pinExpireTime) return '永久置顶'
      const now = new Date()
      const expire = new Date(row.pinExpireTime)
      const diff = expire - now
      if (diff <= 0) return '已过期'
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(hours / 24)
      if (days > 0) return `剩余${days}天${hours % 24}小时`
      if (hours > 0) return `剩余${hours}小时`
      const minutes = Math.floor(diff / (1000 * 60))
      return `剩余${minutes}分钟`
    },
    getList() {
      this.loading = true
      listPost(this.queryParams).then(response => {
        this.postList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.queryParams['user.nickname'] = undefined
      this.queryParams.delFlag = '0'
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.postId)
      this.multiple = !selection.length
    },
    handleView(row) {
      this.detailTitle = '帖子详情'
      this.commentDelFlag = '0'
      getPost(row.postId).then(response => {
        this.detailData = response.data
        this.detailOpen = true
        this.loadComments()
      })
    },
    loadComments() {
      if (!this.detailData) return
      this.commentLoading = true
      listCommentByPost(this.detailData.postId, this.commentDelFlag).then(response => {
        this.commentList = response.data || []
        this.commentLoading = false
      }).catch(() => {
        this.commentLoading = false
      })
    },
    handleDeleteComment(row) {
      this.$modal.confirm('确定要删除该评论吗？').then(() => {
        return delComment(row.commentId)
      }).then(() => {
        this.refreshPostDetail()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleRestoreComment(row) {
      this.$modal.confirm('确定要恢复该评论吗？').then(() => {
        return restoreComment(row.commentId)
      }).then(() => {
        this.refreshPostDetail()
        this.$modal.msgSuccess('恢复成功')
      }).catch(() => {})
    },
    refreshPostDetail() {
      if (!this.detailData) return
      getPost(this.detailData.postId).then(response => {
        this.detailData = response.data
        this.loadComments()
      })
    },
    handleLock(row) {
      this.$modal.confirm('确定要锁定帖子"' + row.title + '"吗？锁定后将无法发表新评论。').then(() => {
        return lockPost(row.postId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('锁定成功')
      }).catch(() => {})
    },
    handleUnlock(row) {
      this.$modal.confirm('确定要解锁帖子"' + row.title + '"吗？').then(() => {
        return unlockPost(row.postId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('解锁成功')
      }).catch(() => {})
    },
    handleDelete(row) {
      const postIds = row.postId || this.ids
      this.$modal.confirm('确定要删除选中的帖子吗？').then(() => {
        return delPost(postIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleRestore(row) {
      this.$modal.confirm('确定要恢复帖子"' + row.title + '"吗？').then(() => {
        return restorePost(row.postId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('恢复成功')
      }).catch(() => {})
    },
    handlePin(row) {
      this.pinPostId = row.postId
      this.pinHours = 0
      this.pinDialogVisible = true
    },
    submitPin() {
      pinPost(this.pinPostId, this.pinHours).then(() => {
        this.pinDialogVisible = false
        this.getList()
        this.$modal.msgSuccess('置顶成功')
      }).catch(() => {})
    },
    handleUnpin(row) {
      this.$modal.confirm('确定要取消置顶帖子"' + row.title + '"吗？').then(() => {
        return unpinPost(row.postId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('取消置顶成功')
      }).catch(() => {})
    },
    handleViewLog(row) {
      this.logDialogVisible = true
      this.logLoading = true
      this.logList = []
      getPostLog(row.postId).then(response => {
        this.logList = response.data || []
      }).finally(() => {
        this.logLoading = false
      })
    },
    getActionLabel(action) {
      const map = {
        'create': '发布',
        'delete': '删除',
        'restore': '恢复',
        'pin': '置顶',
        'unpin': '取消置顶',
        'lock': '锁定',
        'unlock': '解锁'
      }
      return map[action] || action
    },
    handleViewCommentLog() {
      if (!this.detailData) return
      this.commentLogDialogVisible = true
      this.commentLogLoading = true
      this.commentLogList = []
      getCommentLog(this.detailData.postId).then(response => {
        this.commentLogList = response.data || []
      }).finally(() => {
        this.commentLogLoading = false
      })
    }
  }
}
</script>

<style scoped>
.post-detail h2 {
  margin: 0 0 15px 0;
}
.post-meta {
  color: #909399;
  font-size: 14px;
}
.post-meta span {
  margin-right: 20px;
}
.post-content {
  line-height: 1.8;
  white-space: pre-wrap;
}
.post-images {
  margin-top: 15px;
}
.comment-section {
  margin-top: 10px;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.comment-header h4 {
  margin: 0;
  color: #606266;
}
.pin-dialog-content {
  padding: 10px 20px;
}
.pin-hint {
  color: #606266;
  margin-bottom: 20px;
  font-size: 14px;
}
.pin-options {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
}
.post-video {
  margin-top: 15px;
}
.video-embed-wrapper {
  position: relative;
  width: 100%;
  max-width: 640px;
  padding-bottom: 56.25%;
  background: #000;
  border-radius: 4px;
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
.video-link {
  padding: 10px 15px;
  background: #f4f4f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.video-link i {
  font-size: 18px;
  color: #909399;
}
.video-link a {
  color: #409eff;
  word-break: break-all;
}
</style>
