<template>
  <div class="point-rules-page">
    <div class="toolbar">
      <div class="filters">
        <el-select v-model="query.ruleType" clearable placeholder="规则类型" style="width: 160px" @change="fetchList">
          <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px" @change="fetchList">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button @click="fetchList">查询</el-button>
      </div>
      <div class="actions">
        <el-button @click="openLogs()">执行日志</el-button>
        <el-button type="primary" @click="openCreate">新增规则</el-button>
      </div>
    </div>

    <el-card shadow="hover">
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="ruleName" label="规则名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="ruleTypeName" label="规则类型" width="140" />
        <el-table-column label="积分数量" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.pointsAmount >= 0 ? 'points-add' : 'points-sub'">
              {{ row.pointsAmount >= 0 ? '+' : '' }}{{ row.pointsAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="每日限制" width="110" align="center">
          <template #default="{ row }">
            {{ row.maxDailyTimes ?? '不限' }}
          </template>
        </el-table-column>
        <el-table-column label="每月限制" width="110" align="center">
          <template #default="{ row }">
            {{ row.maxMonthlyTimes ?? '不限' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.statusName || (row.status === 1 ? '启用' : '禁用') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="openLogs(row.id)">日志</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="removeRule(row)">删除</el-button>
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
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择规则类型" style="width: 100%">
            <el-option v-for="item in ruleTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分数量" prop="pointsAmount">
          <el-input-number v-model="form.pointsAmount" :min="-9999" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每日限制">
          <el-input-number v-model="form.maxDailyTimes" :min="1" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每月限制">
          <el-input-number v-model="form.maxMonthlyTimes" :min="1" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="触发条件">
          <el-input
            v-model="form.triggerCondition"
            type="textarea"
            :rows="4"
            placeholder='可填 JSON，例如 {"rating_min":4}'
          />
          <div class="field-help">
            <span>可为空；如果填写，会在保存前校验为合法 JSON。</span>
            <el-button link type="primary" @click="helpVisible = true">查看示例</el-button>
          </div>
        </el-form-item>
        <el-form-item label="规则描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入规则描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="saveForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logsVisible" title="规则执行日志" width="980px">
      <div class="log-toolbar">
        <el-input-number v-model="logsQuery.ruleId" :min="1" placeholder="规则ID" style="width: 140px" />
        <el-input-number v-model="logsQuery.roomId" :min="1" placeholder="房屋ID" style="width: 140px" />
        <el-select v-model="logsQuery.executionResult" clearable placeholder="执行结果" style="width: 140px">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-button @click="fetchLogs">查询日志</el-button>
      </div>
      <el-table :data="logs" v-loading="logsLoading" border>
        <el-table-column prop="ruleName" label="规则名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="roomNum" label="房屋编号" min-width="120" />
        <el-table-column prop="userId" label="用户ID" min-width="100" />
        <el-table-column prop="triggerEvent" label="触发事件" min-width="140" />
        <el-table-column prop="pointsAwarded" label="积分" width="100" align="right" />
        <el-table-column label="结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.executionResult === 1 ? 'success' : 'danger'">
              {{ row.executionResultName || (row.executionResult === 1 ? '成功' : '失败') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failureReason" label="失败原因" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" min-width="180" />
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="logsQuery.current"
          :page-size="logsQuery.size"
          layout="prev, pager, next, total"
          :total="logsTotal"
          @current-change="fetchLogs"
        />
      </div>
    </el-dialog>

    <el-dialog v-model="helpVisible" title="触发条件示例" width="760px">
      <el-table :data="conditionExamples" border>
        <el-table-column prop="name" label="规则类型" width="180" />
        <el-table-column prop="example" label="JSON 示例" min-width="260" />
        <el-table-column prop="description" label="说明" min-width="240" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { PageResult, PointRuleLogVO, PointRuleSaveReq, PointRuleVO } from '@/api/point'
import {
  changePointRuleStatus,
  deletePointRule,
  getPointRuleLogPage,
  getPointRulePage,
  savePointRule
} from '@/api/point'

const ruleTypeOptions = [
  { value: 1, label: '报修完成评价' },
  { value: 2, label: '连续签到' },
  { value: 3, label: '社区活动参与' },
  { value: 4, label: '违规行为扣减' },
  { value: 5, label: '自定义事件' }
]

const conditionExamples = [
  { name: '报修完成评价', example: '{"rating_min":4}', description: '评分大于等于 4 分时触发。' },
  { name: '连续签到', example: '{"continuous_days":7}', description: '连续签到达到 7 天时触发。' },
  { name: '社区活动参与', example: '{"activity_type":"volunteer"}', description: '按活动类型匹配。' },
  { name: '违规行为扣减', example: '{"violation_type":"garbage"}', description: '按违规类型匹配。' },
  { name: '自定义事件', example: '{"event_type":"CUSTOM_EVENT"}', description: '匹配自定义事件名。' }
]

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const logsVisible = ref(false)
const logsLoading = ref(false)
const helpVisible = ref(false)

const list = ref<PointRuleVO[]>([])
const total = ref(0)
const logs = ref<PointRuleLogVO[]>([])
const logsTotal = ref(0)

const query = reactive({
  current: 1,
  size: 10,
  ruleType: null as number | null,
  status: null as number | null
})

const logsQuery = reactive({
  current: 1,
  size: 10,
  ruleId: null as number | null,
  roomId: null as number | null,
  executionResult: null as number | null
})

const formRef = ref<FormInstance>()
const form = reactive<PointRuleSaveReq>({
  id: null,
  ruleName: '',
  ruleType: null,
  triggerCondition: '',
  pointsAmount: null,
  maxDailyTimes: null,
  maxMonthlyTimes: null,
  status: 1,
  description: '',
  sortOrder: 0
})

const rules: FormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  pointsAmount: [{ required: true, message: '请输入积分数量', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const dialogTitle = computed(() => (form.id ? '编辑规则' : '新增规则'))

const normalizeNullableNumber = (value: number | null | undefined) =>
  value === null || value === undefined ? null : value

const resetForm = () => {
  Object.assign(form, {
    id: null,
    ruleName: '',
    ruleType: null,
    triggerCondition: '',
    pointsAmount: null,
    maxDailyTimes: null,
    maxMonthlyTimes: null,
    status: 1,
    description: '',
    sortOrder: 0
  })
  formRef.value?.clearValidate()
}

const fetchList = async () => {
  loading.value = true
  try {
    const page = (await getPointRulePage(query)) as PageResult<PointRuleVO>
    list.value = page?.records || []
    total.value = Number(page?.total || 0)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '规则列表加载失败')
  } finally {
    loading.value = false
  }
}

const fetchLogs = async () => {
  logsLoading.value = true
  try {
    const page = (await getPointRuleLogPage(logsQuery)) as PageResult<PointRuleLogVO>
    logs.value = page?.records || []
    logsTotal.value = Number(page?.total || 0)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '执行日志加载失败')
  } finally {
    logsLoading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: PointRuleVO) => {
  Object.assign(form, {
    id: row.id,
    ruleName: row.ruleName,
    ruleType: row.ruleType,
    triggerCondition: row.triggerCondition || '',
    pointsAmount: row.pointsAmount,
    maxDailyTimes: row.maxDailyTimes ?? null,
    maxMonthlyTimes: row.maxMonthlyTimes ?? null,
    status: row.status,
    description: row.description || '',
    sortOrder: row.sortOrder ?? 0
  })
  dialogVisible.value = true
}

const openLogs = (ruleId?: number) => {
  logsVisible.value = true
  logsQuery.current = 1
  logsQuery.ruleId = ruleId ?? null
  fetchLogs()
}

const saveForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (form.triggerCondition && form.triggerCondition.trim()) {
    try {
      JSON.parse(form.triggerCondition)
    } catch {
      ElMessage.error('触发条件必须是合法 JSON')
      return
    }
  }

  saving.value = true
  try {
    await savePointRule({
      ...form,
      maxDailyTimes: normalizeNullableNumber(form.maxDailyTimes),
      maxMonthlyTimes: normalizeNullableNumber(form.maxMonthlyTimes),
      sortOrder: normalizeNullableNumber(form.sortOrder)
    })
    ElMessage.success(form.id ? '规则更新成功' : '规则创建成功')
    dialogVisible.value = false
    await fetchList()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '规则保存失败')
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row: PointRuleVO) => {
  const nextStatus = row.status === 1 ? 0 : 1
  try {
    await changePointRuleStatus(row.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '规则已启用' : '规则已禁用')
    await fetchList()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态切换失败')
  }
}

const removeRule = async (row: PointRuleVO) => {
  try {
    await ElMessageBox.confirm(`确认删除规则“${row.ruleName}”吗？`, '删除规则', { type: 'warning' })
    await deletePointRule(row.id)
    ElMessage.success('规则已删除')
    await fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error instanceof Error ? error.message : '删除失败')
    }
  }
}

fetchList()
</script>

<style scoped>
.point-rules-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.filters,
.actions,
.log-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.points-add {
  color: #16a34a;
  font-weight: 700;
}

.points-sub {
  color: #dc2626;
  font-weight: 700;
}

.field-help {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}
</style>
