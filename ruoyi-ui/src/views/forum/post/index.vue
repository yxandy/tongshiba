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
      <el-form-item label="状态" prop="isLocked">
        <el-select v-model="queryParams.isLocked" placeholder="锁定状态" clearable style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="已锁定" value="1" />
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
      <el-table-column label="状态" prop="isLocked" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isLocked === '1' ? 'danger' : 'success'">
            {{ scope.row.isLocked === '1' ? '已锁定' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" prop="createTime" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleView(scope.row)">查看</el-button>
          <el-button size="mini" type="text" v-if="scope.row.isLocked === '0'" @click="handleLock(scope.row)" v-hasPermi="['forum:post:lock']">锁定</el-button>
          <el-button size="mini" type="text" v-else @click="handleUnlock(scope.row)" v-hasPermi="['forum:post:lock']">解锁</el-button>
          <el-button size="mini" type="text" style="color: #f56c6c" @click="handleDelete(scope.row)" v-hasPermi="['forum:post:remove']">删除</el-button>
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

        <el-divider />
        <!-- 评论区域（预留） -->
        <div class="comment-section">
          <h4>评论列表（共 {{ detailData.commentCount }} 条）</h4>
          <el-empty description="评论管理功能开发中..." />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPost, getPost, delPost, lockPost, unlockPost } from '@/api/forum/post'

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
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        'user.nickname': undefined,
        userUnit: undefined,
        userDept: undefined,
        isLocked: undefined
      }
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
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.postId)
      this.multiple = !selection.length
    },
    handleView(row) {
      this.detailTitle = '帖子详情'
      getPost(row.postId).then(response => {
        this.detailData = response.data
        this.detailOpen = true
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
.comment-section h4 {
  margin-bottom: 15px;
  color: #606266;
}
</style>
