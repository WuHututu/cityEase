<template>
  <div class="page">
    <div class="toolbar">
      <div class="left">
        <el-select v-model="query.ruleType" placeholder="规则类型" style="width: 150px" clearable @change="fetchList">
          <el-option :value="1" label="报修完成评价" />
          <el-option :value="2" label="连续签到" />
          <el-option :value="3" label="社区活动参与" />
          <el-option :value="4" label="违规行为扣减" />
          <el-option :value="5" label="自定义事件" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" style="width: 120px" clearable @change="fetchList">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
        <el-button @click="fetchList">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>
      <div class="right">
        <el-button type="primary" @click="openCreate">新增规则</el-button>
        <el-button @click="showLogs">执行日志</el-button>
      </div>
    </div>

    <el-table :data="list" border style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="ruleName" label="规则名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="ruleTypeName" label="规则类型" width="130" />
      <el-table-column prop="pointsAmount" label="积分数量" width="100" align="right">
        <template #default="{ row }">
          <span :class="row.pointsAmount > 0 ? 'points-add' : 'points-minus'">
            {{ row.pointsAmount > 0 ? '+' : '' }}{{ row.pointsAmount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="maxDailyTimes" label="每日限制" width="100" align="center">
        <template #default="{ row }">
          {{ row.maxDailyTimes || '无限制' }}
        </template>
      </el-table-column>
      <el-table-column prop="maxMonthlyTimes" label="每月限制" width="100" align="center">
        <template #default="{ row }">
          {{ row.maxMonthlyTimes || '无限制' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination 
        v-model:current-page="query.current" 
        :page-size="query.size" 
        layout="prev, pager, next, total"
        :total="total" 
        @current-change="fetchList" 
      />
    </div>

    <!-- 新增/编辑规则弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px" 
      @close="resetForm"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px"
      >
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择规则类型" style="width: 100%">
            <el-option :value="1" label="报修完成评价" />
            <el-option :value="2" label="连续签到" />
            <el-option :value="3" label="社区活动参与" />
            <el-option :value="4" label="违规行为扣减" />
            <el-option :value="5" label="自定义事件" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="积分数量" prop="pointsAmount">
          <el-input-number 
            v-model="form.pointsAmount" 
            :min="-9999" 
            :max="9999"
            placeholder="正数为奖励，负数为扣减"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="每日限制" prop="maxDailyTimes">
          <el-input-number 
            v-model="form.maxDailyTimes" 
            :min="1"
            placeholder="不填表示无限制"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="每月限制" prop="maxMonthlyTimes">
          <el-input-number 
            v-model="form.maxMonthlyTimes" 
            :min="1"
            placeholder="不填表示无限制"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="触发条件" prop="triggerCondition">
          <el-input 
            v-model="form.triggerCondition" 
            type="textarea" 
            :rows="4"
            placeholder="JSON格式的触发条件，如：{&quot;rating_min&quot;: 4}"
          />
          <div class="form-help">
            <el-link type="primary" @click="showConditionHelp">查看条件配置示例</el-link>
          </div>
        </el-form-item>
        
        <el-form-item label="规则描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入规则描述"
          />
        </el-form-item>
        
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number 
            v-model="form.sortOrder" 
            :min="0"
            placeholder="数字越小排序越靠前"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveForm" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 条件配置帮助弹窗 -->
    <el-dialog v-model="helpVisible" title="触发条件配置示例" width="700px">
      <div class="help-content">
        <el-tabs>
          <el-tab-pane label="报修完成评价">
            <pre><code>{"rating_min": 4}</code></pre>
            <p>评价星级最低要求为4星</p>
          </el-tab-pane>
          <el-tab-pane label="连续签到">
            <pre><code>{"continuous_days": 7}</code></pre>
            <p>连续签到天数要求为7天</p>
          </el-tab-pane>
          <el-tab-pane label="社区活动参与">
            <pre><code>{"activity_type": "volunteer"}</code></pre>
            <p>活动类型要求为志愿活动</p>
          </el-tab-pane>
          <el-tab-pane label="违规行为扣减">
            <pre><code>{"violation_type": "garbage"}</code></pre>
            <p>违规类型要求为垃圾投放违规</p>
          </el-tab-pane>
          <el-tab-pane label="自定义事件">
            <pre><code>{"event_type": "CUSTOM_EVENT"}</code></pre>
            <p>自定义事件类型</p>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import * as pointApi from '@/api/point'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const helpVisible = ref(false)
const list = ref([])
const total = ref(0)

// 查询参数
const query = reactive({
  current: 1,
  size: 20,
  ruleType: null,
  status: null
})

// 表单数据
const formRef = ref()
const form = reactive({
  id: null,
  ruleName: '',
  ruleType: null,
  pointsAmount: null,
  maxDailyTimes: null,
  maxMonthlyTimes: null,
  triggerCondition: '',
  description: '',
  sortOrder: 0,
  status: 1
})

// 表单验证规则
const rules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' }
  ],
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  pointsAmount: [
    { required: true, message: '请输入积分数量', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑规则' : '新增规则'
})

// 获取规则列表
const fetchList = async () => {
  try {
    loading.value = true
    const response = await pointApi.getPointRulePage(query)
    // response直接就是Page对象
    list.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('获取规则列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗
const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    ruleName: '',
    ruleType: null,
    pointsAmount: null,
    maxDailyTimes: null,
    maxMonthlyTimes: null,
    triggerCondition: '',
    description: '',
    sortOrder: 0,
    status: 1
  })
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 保存表单
const saveForm = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    await pointApi.savePointRule(form)
    ElMessage.success(form.id ? '修改成功' : '新增成功')
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    if (error.fields) {
      // 表单验证错误
      return
    }
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 切换状态
const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const action = newStatus === 1 ? '启用' : '禁用'
    
    await ElMessageBox.confirm(`确定要${action}规则"${row.ruleName}"吗？`, '确认操作', {
      type: 'warning'
    })
    
    await pointApi.changePointRuleStatus(row.id, newStatus)
    
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error('操作失败: ' + error.message)
  }
}

// 删除规则
const onDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除规则"${row.ruleName}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await pointApi.deletePointRule(row.id)
    
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error('删除失败: ' + error.message)
  }
}

// 显示条件帮助
const showConditionHelp = () => {
  helpVisible.value = true
}

// 显示执行日志
const showLogs = () => {
  // 这里可以跳转到日志页面或打开日志弹窗
  ElMessage.info('执行日志功能开发中...')
}

// 初始化
onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.page {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.toolbar .left {
  display: flex;
  gap: 10px;
  align-items: center;
}

.points-add {
  color: #67c23a;
  font-weight: bold;
}

.points-minus {
  color: #f56c6c;
  font-weight: bold;
}

.form-help {
  margin-top: 5px;
  font-size: 12px;
}

.help-content {
  padding: 10px 0;
}

.help-content pre {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  margin: 10px 0;
  overflow-x: auto;
}

.help-content code {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.help-content p {
  margin: 10px 0;
  color: #666;
}

.pager {
  margin-top: 20px;
  text-align: center;
}
</style>
