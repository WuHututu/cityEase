<template>
  <div class="house-container">
    <!-- 顶部搜索区域 -->
    <div class="search-wrapper">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="区域类型">
          <el-select v-model="queryParams.areaType" placeholder="请选择区域类型" clearable style="width: 160px;">
            <el-option label="房屋" :value="1" />
            <el-option label="公共区域" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="区域名称">
          <el-input
            v-model="queryParams.areaName"
            placeholder="请输入区域名称"
            clearable
            style="width: 200px;"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 主体表格区域 -->
    <div class="table-wrapper">
      <el-table
          v-loading="loading"
          :data="tableData"
          style="width: 100%"
          class="custom-table"
          header-row-class-name="custom-table-header"
      >
        <el-table-column prop="id" label="ID" width="100" align="center" />

        <el-table-column label="区域类型" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.areaType === 1" type="success">房屋</el-tag>
            <el-tag v-else type="info">公共区域</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="areaName" label="区域名称" min-width="180" />

        <el-table-column prop="areaAddress" label="详细地址" min-width="200" />

        <el-table-column prop="createTime" label="创建时间" width="180" />

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button
                size="small"
                type="primary"
                plain
                @click="viewDetail(scope.row)"
            >
              查看
            </el-button>
            <el-button
                size="small"
                type="info"
                plain
                @click="editArea(scope.row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无区域数据" />
        </template>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.pageNo"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :background="true"
            layout="prev, pager, jumper, next, ->, total"
            :total="total"
            @size-change="handleSearch"
            @current-change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// --- 搜索与分页状态 ---
const loading = ref(false)
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  areaType: null as number | null,
  areaName: ''
})

const tableData = ref<any[]>([])

// --- 获取列表数据 ---
const fetchData = async () => {
  loading.value = true
  try {
    // 增加兼容性参数 (current, size)，解决后端如果是 MyBatis-Plus Page 默认分页解析不到页码的问题
    const params = {
      ...queryParams,
      current: queryParams.pageNo,
      size: queryParams.pageSize
    }

    const res: any = await request.post('/admin/pms/house/page', params)
    const pageData = res.data ? res.data : res
    tableData.value = pageData.records || []
    total.value = parseInt(pageData.total) || 0
  } catch (error) {
    console.error('获取区域列表失败', error)
    ElMessage.error('获取区域列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索与重置
const handleSearch = () => {
  queryParams.pageNo = 1
  fetchData()
}
const resetQuery = () => {
  queryParams.areaType = null
  queryParams.areaName = ''
  handleSearch()
}

// 查看详情
const viewDetail = (row: any) => {
  ElMessage.info(`查看区域 ${row.areaName} 详情`)
}

// 编辑区域
const editArea = (row: any) => {
  ElMessage.info(`编辑区域 ${row.areaName}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.house-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 搜索区域暗黑风格定制 */
.search-wrapper {
  background-color: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 20px 20px 0 20px;

  /* 深度穿透修改 Element Plus 表单组件样式 */
  :deep(.el-form-item__label) {
    color: #94a3b8;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select .el-input__wrapper) {
    background-color: rgba(15, 23, 42, 0.6);
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  }

  :deep(.el-input__inner) {
    color: #e2e8f0;
  }
}

/* 表格区域暗黑风格定制 */
.table-wrapper {
  flex: 1;
  background-color: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;

  /* 表格基础样式穿透 */
  :deep(.custom-table) {
    background-color: transparent;
    --el-table-border-color: rgba(255, 255, 255, 0.05);
    --el-table-row-hover-bg-color: rgba(24, 144, 255, 0.1);

    /* --- 关键修复：为固定列添加实色背景 --- */
    .el-table__fixed-right,
    .el-table__fixed {
      /* 必须使用实色，否则滚动时下方内容会透出来 */
      /* #1e293b 是 rgba(30, 41, 59) 的实色表现 */
      background-color: #1e293b !important;
      height: calc(100% - 12px) !important; /* 避开滚动条高度，防止遮挡横向滚动条 */
    }

    /* 针对固定列中的单元格 */
    .el-table__cell.is-fixed-right,
    .el-table__cell.is-fixed-left {
      background-color: #1e293b !important;
    }

    /* 修复固定列顶部的表头背景 */
    th.el-table__fixed-right,
    th.el-table__fixed-left {
      background-color: #161e2c !important;
    }

    th.el-table__cell {
      background-color: rgba(15, 23, 42, 0.8) !important;
      color: #cbd5e1;
      font-weight: 600;
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }

    tr {
      background-color: transparent;
    }

    td.el-table__cell {
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
      color: #94a3b8;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;

    :deep(.el-pagination.is-background .el-pager li) {
      background-color: rgba(15, 23, 42, 0.6);
      color: #94a3b8;
    }
    :deep(.el-pagination.is-background .el-pager li.is-active) {
      background-color: #1890ff;
      color: #fff;
    }
    :deep(.el-pagination.is-background .btn-next),
    :deep(.el-pagination.is-background .btn-prev) {
      background-color: rgba(15, 23, 42, 0.6);
      color: #94a3b8;
    }
  }
}
</style>