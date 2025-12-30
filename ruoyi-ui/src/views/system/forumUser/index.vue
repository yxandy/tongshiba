<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户昵称" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          placeholder="请输入用户昵称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="单位" prop="unit">
        <el-select v-model="queryParams.unit" placeholder="请选择单位" clearable @change="handleUnitChange">
          <el-option
            v-for="unit in unitList"
            :key="unit.unitId"
            :label="unit.displayName"
            :value="unit.unitName"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="部门" prop="department">
        <el-select v-model="queryParams.department" placeholder="请选择部门" clearable :disabled="!selectedUnitId">
          <el-option
            v-for="dept in deptList"
            :key="dept.deptId"
            :label="dept.deptName"
            :value="dept.deptName"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="queryParams.role" placeholder="请选择角色" clearable>
          <el-option label="管理员" value="admin" />
          <el-option label="分级管理员" value="sub_admin" />
          <el-option label="普通用户" value="user" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['system:forumUser:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList">
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="昵称" align="center" prop="nickname" :show-overflow-tooltip="true" />
      <el-table-column label="企业微信ID" align="center" prop="wxUserid" :show-overflow-tooltip="true" />
      <el-table-column label="单位" align="center" prop="unit" :show-overflow-tooltip="true" />
      <el-table-column label="部门" align="center" prop="department" :show-overflow-tooltip="true" />
      <el-table-column label="角色" align="center" prop="role" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.role === 'admin'" type="danger">管理员</el-tag>
          <el-tag v-else-if="scope.row.role === 'sub_admin'" type="warning">分级管理员</el-tag>
          <el-tag v-else type="info">普通用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="限流状态" align="center" prop="isRateLimited" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isRateLimited === '1' ? 'danger' : 'success'">
            {{ scope.row.isRateLimited === '1' ? '已限流' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-user"
            @click="handleAssignRole(scope.row)"
            v-hasPermi="['system:forumUser:assignRole']"
          >分配角色</el-button>
          <el-button
            size="mini"
            type="text"
            :icon="scope.row.isRateLimited === '1' ? 'el-icon-unlock' : 'el-icon-lock'"
            @click="handleRateLimit(scope.row)"
            v-hasPermi="['system:forumUser:rateLimit']"
          >{{ scope.row.isRateLimited === '1' ? '解除限流' : '限流' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 分配角色对话框 -->
    <el-dialog title="分配角色" :visible.sync="roleDialogVisible" width="500px" append-to-body>
      <el-form ref="roleForm" :model="roleForm" label-width="100px">
        <el-form-item label="用户昵称">
          <el-input v-model="roleForm.nickname" disabled />
        </el-form-item>
        <el-form-item label="选择角色" prop="role">
          <el-radio-group v-model="roleForm.role">
            <el-radio label="user">普通用户</el-radio>
            <el-radio label="sub_admin">分级管理员</el-radio>
            <el-radio label="admin">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRole">确 定</el-button>
        <el-button @click="roleDialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listForumUser, assignRole, setRateLimit, getUnitList, getDeptList } from "@/api/system/forumUser"

export default {
  name: "ForumUser",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      userList: [],
      unitList: [],
      deptList: [],
      selectedUnitId: null,
      roleDialogVisible: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        nickname: undefined,
        unit: undefined,
        department: undefined,
        role: undefined
      },
      roleForm: {
        userId: null,
        nickname: '',
        role: 'user'
      }
    }
  },
  created() {
    this.getList()
    this.loadUnitList()
  },
  methods: {
    getList() {
      this.loading = true
      listForumUser(this.queryParams).then(response => {
        this.userList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    loadUnitList() {
      getUnitList().then(response => {
        this.unitList = response.data
      })
    },
    handleUnitChange(unitName) {
      const unit = this.unitList.find(u => u.unitName === unitName)
      if (unit) {
        this.selectedUnitId = unit.unitId
        getDeptList(unit.unitId).then(response => {
          this.deptList = response.data
        })
      } else {
        this.selectedUnitId = null
        this.deptList = []
      }
      this.queryParams.department = undefined
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.selectedUnitId = null
      this.deptList = []
      this.handleQuery()
    },
    handleAssignRole(row) {
      this.roleForm = {
        userId: row.userId,
        nickname: row.nickname,
        role: row.role || 'user'
      }
      this.roleDialogVisible = true
    },
    submitRole() {
      assignRole(this.roleForm).then(response => {
        this.$modal.msgSuccess("角色分配成功")
        this.roleDialogVisible = false
        this.getList()
      })
    },
    handleRateLimit(row) {
      const newStatus = row.isRateLimited === '1' ? '0' : '1'
      const action = newStatus === '1' ? '限流' : '解除限流'
      this.$modal.confirm(`是否确认${action}用户"${row.nickname}"?`).then(() => {
        return setRateLimit({
          userId: row.userId,
          isRateLimited: newStatus
        })
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess(`${action}成功`)
      }).catch(() => {})
    },
    handleExport() {
      this.download('system/forum/user/export', {
        ...this.queryParams
      }, `forum_user_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
