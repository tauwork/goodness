<template>
  <div>
    <div class="kpis">
      <div class="kpi"><span>未收合计</span><b>¥ {{ money(outstanding) }}</b></div>
      <div class="kpi"><span>待收款</span><b>{{ rows.filter(r => r.status === 'OPEN').length }}</b></div>
      <div class="kpi"><span>部分收款</span><b>{{ rows.filter(r => r.status === 'PARTIAL').length }}</b></div>
      <div class="kpi"><span>已结清</span><b>{{ rows.filter(r => r.status === 'SETTLED').length }}</b></div>
    </div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px">
          <b>应收账单</b>
          <el-select v-model="status" clearable placeholder="全部状态" style="width:140px;margin-left:auto" @change="load">
            <el-option label="待收款" value="OPEN" />
            <el-option label="部分收款" value="PARTIAL" />
            <el-option label="已结清" value="SETTLED" />
            <el-option label="已作废" value="VOID" />
          </el-select>
          <el-button type="primary" @click="openCreate">新建账单</el-button>
        </div>
      </template>
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="title" label="账单" min-width="160" show-overflow-tooltip />
        <el-table-column label="客户" min-width="150">
          <template #default="{ row }">{{ row.customerName || '未命名' }}<div style="color:#8A94A8;font-size:12px">{{ row.customerPhone || '未留手机' }}</div></template>
        </el-table-column>
        <el-table-column label="应收" width="110">
          <template #default="{ row }">¥ {{ money(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="已收" width="110">
          <template #default="{ row }">¥ {{ money(row.received) }}</template>
        </el-table-column>
        <el-table-column label="未收" width="110">
          <template #default="{ row }">¥ {{ money(Number(row.amount) - Number(row.received || 0)) }}</template>
        </el-table-column>
        <el-table-column label="到期" width="110">
          <template #default="{ row }">{{ row.dueDate || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'OPEN' || row.status === 'PARTIAL'" size="small" link type="primary" @click="receive(row)">收款</el-button>
            <el-button v-if="Number(row.received || 0) === 0 && row.status !== 'VOID'" size="small" link @click="voidBill(row)">作废</el-button>
            <el-button v-if="Number(row.received || 0) === 0" size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog" title="新建账单" width="520px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="账单名称" required>
          <el-input v-model="form.title" maxlength="128" />
        </el-form-item>
        <el-form-item label="客户" required>
          <el-select v-model="form.customerId" filterable remote reserve-keyword :remote-method="searchCustomer" placeholder="搜姓名或手机号" style="width:100%">
            <el-option v-for="item in customers" :key="item.id" :label="labelOf(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="应收金额" required>
          <el-input v-model="form.amount" placeholder="0.00" />
        </el-form-item>
        <el-form-item label="到期日">
          <el-date-picker v-model="form.dueDate" value-format="YYYY-MM-DD" type="date" placeholder="可空" style="width:100%" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.remark" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const rows = ref([])
const customers = ref([])
const loading = ref(false)
const dialog = ref(false)
const form = ref({})
const status = ref('')

function money(v) { return Number(v || 0).toFixed(2) }
function labelOf(item) { return `${item.name || '未命名'} ${item.phone || ''}`.trim() }
const statusText = s => ({ OPEN: '待收款', PARTIAL: '部分收款', SETTLED: '已结清', VOID: '已作废' }[s] || s)
const statusType = s => ({ OPEN: 'warning', PARTIAL: 'primary', SETTLED: 'success', VOID: 'info' }[s])
const outstanding = computed(() => rows.value
  .filter(r => r.status === 'OPEN' || r.status === 'PARTIAL')
  .reduce((sum, r) => sum + Number(r.amount || 0) - Number(r.received || 0), 0))

async function load() {
  loading.value = true
  try { rows.value = await http.get('/admin/finance/receivable/list', { params: { status: status.value || undefined } }) }
  finally { loading.value = false }
}

async function searchCustomer(keyword) {
  const page = await http.get('/admin/customer/page', { params: { page: 1, size: 20, keyword: keyword || undefined } })
  customers.value = page.records || []
}

function openCreate() {
  form.value = { title: '', customerId: null, amount: '', dueDate: '', remark: '' }
  dialog.value = true
  searchCustomer('')
}

async function save() {
  const amount = Number(form.value.amount)
  if (!form.value.title?.trim()) return ElMessage.warning('请填写账单名称')
  if (!form.value.customerId) return ElMessage.warning('请选择客户')
  if (!(amount > 0)) return ElMessage.warning('请填写应收金额')
  await http.post('/admin/finance/receivable', { ...form.value, amount, dueDate: form.value.dueDate || null })
  ElMessage.success('已保存')
  dialog.value = false
  load()
}

async function receive(row) {
  const remain = (Number(row.amount) - Number(row.received || 0)).toFixed(2)
  const { value } = await ElMessageBox.prompt(`未收 ¥${remain}`, '登记收款', { inputValue: remain, inputPattern: /^(?:0|[1-9]\d*)(?:\.\d{1,2})?$/, inputErrorMessage: '请填写金额' })
  await http.post(`/admin/finance/receivable/${row.id}/receive`, { amount: Number(value) })
  ElMessage.success('已收款')
  load()
}

async function voidBill(row) {
  await ElMessageBox.confirm(`作废账单「${row.title}」？`, '作废')
  await http.post(`/admin/finance/receivable/${row.id}/void`)
  ElMessage.success('已作废')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`删除账单「${row.title}」？`, '删除')
  await http.delete(`/admin/finance/receivable/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.kpis { display:flex; gap:12px; margin-bottom:14px; }
.kpi { flex:1; background:#fff; border-radius:12px; padding:14px 16px; }
.kpi span { color:#8A94A8; font-size:12px; }
.kpi b { display:block; font-size:22px; margin-top:4px; }
</style>
