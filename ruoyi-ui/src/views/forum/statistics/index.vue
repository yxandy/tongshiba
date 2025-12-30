<template>
  <div class="app-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb-nav" v-if="currentLevel > 1">
      <el-breadcrumb-item>
        <el-link @click="backToUnitLevel">单位统计</el-link>
      </el-breadcrumb-item>
      <el-breadcrumb-item v-if="currentLevel === 2">
        {{ currentUnitName }} - {{ viewMode === 'dept' ? '部门统计' : '人员统计' }}
      </el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="90px">
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
          v-model="queryParams.startDate"
          type="date"
          placeholder="选择开始日期"
          value-format="yyyy-MM-dd"
          style="width: 150px"
        />
      </el-form-item>
      <el-form-item label="截止日期" prop="endDate">
        <el-date-picker
          v-model="queryParams.endDate"
          type="date"
          placeholder="选择截止日期"
          value-format="yyyy-MM-dd"
          style="width: 150px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" @click="handleExport" v-hasPermi="['forum:statistics:export']">导出</el-button>
      </el-col>
      <!-- 第二层：视图切换 -->
      <el-col :span="6" v-if="currentLevel === 2">
        <el-radio-group v-model="viewMode" size="small" @change="handleViewModeChange">
          <el-radio-button label="dept">按部门</el-radio-button>
          <el-radio-button label="user">按人员</el-radio-button>
        </el-radio-group>
      </el-col>
    </el-row>

    <!-- 第一层：单位统计表格 -->
    <el-table v-if="currentLevel === 1" v-loading="loading" :data="statsList" border>
      <el-table-column label="单位名称" prop="unitName" min-width="200">
        <template slot-scope="scope">
          <el-link type="primary" @click="drillDown(scope.row)">{{ scope.row.unitName || '(未知单位)' }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="发帖数量" prop="postCount" width="120" align="center" />
      <el-table-column label="置顶数量" prop="pinCount" width="120" align="center" />
    </el-table>

    <!-- 第二层：部门/人员统计表格 -->
    <el-table v-if="currentLevel === 2" v-loading="loading" :data="statsList" border>
      <el-table-column :label="viewMode === 'dept' ? '部门名称' : '发帖人'" min-width="200">
        <template slot-scope="scope">
          {{ viewMode === 'dept' ? (scope.row.deptName || '(未知部门)') : (scope.row.userName || '(未知用户)') }}
        </template>
      </el-table-column>
      <el-table-column label="发帖数量" prop="postCount" width="120" align="center" />
      <el-table-column label="置顶数量" prop="pinCount" width="120" align="center" />
    </el-table>

    <!-- 统计提示 -->
    <div class="stats-tip" v-if="!loading">
      <el-alert type="info" :closable="false" show-icon>
        <template slot="title">
          统计说明：已排除锁定、限流、删除的帖子
        </template>
      </el-alert>
    </div>
  </div>
</template>

<script>
import { listUnitStats, listDeptStats, listUserStats } from '@/api/forum/statistics'

export default {
  name: 'ForumStatistics',
  data() {
    return {
      // 遮罩层
      loading: false,
      // 当前层级：1-单位, 2-部门/人员
      currentLevel: 1,
      // 当前单位名称（用于第二层）
      currentUnitName: '',
      // 视图模式：dept-按部门, user-按人员
      viewMode: 'dept',
      // 统计数据列表
      statsList: [],
      // 查询参数
      queryParams: {
        startDate: null,
        endDate: null,
        unitName: null
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询统计数据 */
    getList() {
      this.loading = true
      if (this.currentLevel === 1) {
        listUnitStats(this.queryParams).then(response => {
          this.statsList = response.data || []
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      } else {
        const query = { ...this.queryParams, unitName: this.currentUnitName }
        const apiCall = this.viewMode === 'dept' ? listDeptStats : listUserStats
        apiCall(query).then(response => {
          this.statsList = response.data || []
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      }
    },
    /** 搜索按钮 */
    handleQuery() {
      this.getList()
    },
    /** 重置按钮 */
    resetQuery() {
      this.queryParams = {
        startDate: null,
        endDate: null,
        unitName: null
      }
      this.getList()
    },
    /** 钻取到第二层 */
    drillDown(row) {
      this.currentLevel = 2
      this.currentUnitName = row.unitName
      this.viewMode = 'dept'
      this.getList()
    },
    /** 返回第一层 */
    backToUnitLevel() {
      this.currentLevel = 1
      this.currentUnitName = ''
      this.viewMode = 'dept'
      this.getList()
    },
    /** 视图模式切换 */
    handleViewModeChange() {
      this.getList()
    },
    /** 导出按钮 */
    handleExport() {
      let url = ''
      let filename = ''
      if (this.currentLevel === 1) {
        url = 'forum/statistics/export/unit'
        filename = `单位发帖统计_${new Date().getTime()}.xlsx`
      } else {
        if (this.viewMode === 'dept') {
          url = 'forum/statistics/export/dept'
          filename = `${this.currentUnitName}_部门统计_${new Date().getTime()}.xlsx`
        } else {
          url = 'forum/statistics/export/user'
          filename = `${this.currentUnitName}_人员统计_${new Date().getTime()}.xlsx`
        }
      }
      const params = this.currentLevel === 1 ? this.queryParams : { ...this.queryParams, unitName: this.currentUnitName }
      this.download(url, params, filename)
    }
  }
}
</script>

<style scoped>
.breadcrumb-nav {
  margin-bottom: 15px;
}
.stats-tip {
  margin-top: 15px;
}
</style>
