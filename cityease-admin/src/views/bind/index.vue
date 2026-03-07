<template>
  <div class="page">
    <el-card shadow="never" class="toolbar">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="业主姓名">
          <el-input v-model="queryParams.ownerName" placeholder="请输入业主姓名" clearable style="width:200px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width:180px" />
        </el-form-item>
        <el-form-item label="房屋关键字">
          <el-input v-model="queryParams.roomKeyword" placeholder="房号/楼栋" clearable style="width:200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:160px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" border style="width:100%">
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column prop="ownerName" label="业主姓名" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="roomInfo" label="房屋信息" min-width="200" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button v-if="row.status===0" size="small" type="success" plain @click="approve(row)">通过</el-button>
            <el-button v-if="row.status===0" size="small" type="warning" plain @click="reject(row)">拒绝</el-button>
            <el-button size="small" type="info" plain @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          :page-size="queryParams.pageSize"
          layout="prev, pager, next, jumper, ->, total"
          :total="total"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="申请详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="业主姓名">{{ detailData.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailData.phone }}</el-descriptions-item>
        <el-descriptions-item label="房屋信息" :span="2">{{ detailData.roomInfo }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">{{ statusText(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detailData.auditorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detailData.auditTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const unwrap = (res:any)=>res?.result ?? res?.data?.data ?? res?.data ?? res

const loading = ref(false)
const total = ref(0)
const tableData = ref<any[]>([])

const queryParams = reactive({
  pageNo:1,
  pageSize:10,
  ownerName:'',
  phone:'',
  roomKeyword:'',
  status:undefined as number|undefined
})

const fetchData = async () => {
  loading.value = true
  try{
    const res:any = await request.post('/admin/pms/bind/page', {...queryParams})
    const pageData = unwrap(res)
    tableData.value = pageData?.records||[]
    total.value = Number(pageData?.total||0)
  }catch{
    tableData.value=[]
    total.value=0
  }finally{
    loading.value=false
  }
}

const handleSearch=()=>{ queryParams.pageNo=1; fetchData() }
const handleReset=()=>{ queryParams.ownerName=''; queryParams.phone=''; queryParams.roomKeyword=''; queryParams.status=undefined; fetchData() }

const statusText=(s:number)=>s===0?'待审核':s===1?'已通过':s===2?'已拒绝':'未知'
const statusTag=(s:number)=>s===0?'warning':s===1?'success':'danger'

const detailVisible=ref(false)
const detailData=ref<any>(null)
const viewDetail=(row:any)=>{ detailData.value=row; detailVisible.value=true }

const approve=async(row:any)=>{
  try{
    await ElMessageBox.confirm('确定通过该申请？','提示',{type:'warning'})
    const res:any = await request.post('/admin/pms/bind/approve',{id:row.id})
    ElMessage.success(unwrap(res)||'操作成功')
    fetchData()
  }catch{}
}

const reject=async(row:any)=>{
  try{
    await ElMessageBox.prompt('请输入拒绝原因','提示',{inputType:'textarea', confirmButtonText:'提交', cancelButtonText:'取消'})
      .then(async({value})=>{
        const res:any = await request.post('/admin/pms/bind/reject',{id:row.id,remark:value})
        ElMessage.success(unwrap(res)||'操作成功')
        fetchData()
      })
  }catch{}
}

fetchData()
</script>

<style scoped>
.page{display:flex;flex-direction:column;gap:12px}
.toolbar{margin-bottom:0}
.pager{display:flex;justify-content:flex-end;padding-top:12px}
</style>
