<template>
  <div class="page">
    <el-card shadow="never" class="toolbar">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="类型">
          <el-select v-model="queryParams.noticeType" placeholder="全部" clearable>
            <el-option label="通知" :value="1" />
            <el-option label="活动" :value="2" />
            <el-option label="提示" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="标题关键字" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="margin-left:auto">
          <el-button type="primary" plain @click="openAddDialog">新增公告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column prop="noticeTitle" label="标题" min-width="240" />
        <el-table-column prop="noticeType" label="类型" width="110" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="320">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="editNotice(row)">编辑</el-button>
            <el-button size="small" type="info" plain @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="success" plain v-if="row.status===0" @click="publish(row)">发布</el-button>
            <el-button size="small" type="warning" plain v-else @click="withdraw(row)">撤回</el-button>
            <el-button size="small" type="danger" plain @click="deleteNotice(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="queryParams.pageNo" :page-size="queryParams.pageSize"
          layout="prev, pager, next, jumper, ->, total" :total="total" @current-change="fetchData" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import request from '@/utils/request'
const unwrap=(res:any)=>res?.result ?? res?.data?.data ?? res?.data ?? res

const tableData=ref<any[]>([])
const total=ref(0)
const loading=ref(false)
const queryParams=reactive({pageNo:1,pageSize:10,noticeType:undefined as number|undefined,status:undefined as number|undefined,keyword:''})

const fetchData=async()=>{
  loading.value=true
  try{ const res:any=await request.post('/admin/system/notice/page',{...queryParams}); tableData.value=unwrap(res)?.records||[]; total.value=unwrap(res)?.total||0 }finally{loading.value=false}
}
const handleSearch=()=>{ queryParams.pageNo=1; fetchData() }
const handleReset=()=>{ queryParams.noticeType=undefined; queryParams.status=undefined; queryParams.keyword=''; fetchData() }
const editNotice=(row:any)=>{}
const viewDetail=(row:any)=>{}
const publish=async(row:any)=>{ await request.post('/admin/system/notice/publish',{id:row.id}); fetchData() }
const withdraw=async(row:any)=>{ await request.post('/admin/system/notice/withdraw',{id:row.id}); fetchData() }
const deleteNotice=async(row:any)=>{ await request.post('/admin/system/notice/delete',{id:row.id}); fetchData() }
fetchData()
</script>

<style scoped>
.page{display:flex;flex-direction:column;gap:12px}
.pager{display:flex;justify-content:flex-end;padding-top:12px}
</style>
