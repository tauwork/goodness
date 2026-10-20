<template>
  <div>
    <div class="kpis">
      <div class="kpi"><span>已确认收入</span><b>¥ {{ money(confirmed) }}</b></div>
      <div class="kpi"><span>结算笔数</span><b>{{ rows.filter(r => r.status === 'CONFIRMED').length }}</b></div>
      <div class="kpi"><span>已作废</span><b>{{ rows.filter(r => r.status === 'VOID').length }}</b></div>
    </div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px">
          <b>收入结算</b>
          <span style="color:#8A94A8;font-size:12px">确认一笔已经入账的收入</span>
          <el-select v-model="status" clearable placeholder="全部状态" style="width:140px;margin-left:auto" @change="load">
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已作废" value="VOID" />
          </el-select>
          <el-button type="primary" @click="openCreate">登记结算</el-button>
        </div>
      </template>
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column prop="title" label="结算名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="客户" min-width="150">
          <template #default="{ row }">
            <span v-if="row.customerId">{{ row.customerName || '未命名' }}</span>
            <span v-else style="color:#C0C4CC">未关联</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">¥ {{ money(row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="settleDate" label="结算日" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="row.status === 'CONFIRMED' ? 'success' : 'info'" size="small">{{ row.status === 'CONFIRMED' ? '已确认' : '已作废' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'CONFIRMED'" size="small" link type="danger" @click="voidRow(row)">作废</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog" title="登记结算" width="520px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="结算名称" required>
          <el-input v-model="form.title" maxlength="128" placeholder="例如 10月服务收入" />
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="form.customerId" filterable remote clearable reserve-keyword :remote-method="searchCustomer" placeholder="可空" style="width:100%">
            <el-option v-for="item in customers" :key="item.id" :label="labelOf(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" required>
          <el-input v-model="form.amount" placeholder="0.00" />
        </el-form-item>
        <el-form-item label="结算日" required>
          <el-date-picker v-model="form.settleDate" value-format="YYYY-MM-DD" type="date" style="width:100%" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.remark" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">确认入账</el-button>
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
function today() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
const confirmed = computed(() => rows.value.filter(r => r.status === 'CONFIRMED').reduce((sum, r) => sum + Number(r.amount || 0), 0))

async function load() {
  loading.value = true
  try { rows.value = await http.get('/admin/finance/settlement/list', { params: { status: status.value || undefined } }) }
  finally { loading.value = false }
}

async function searchCustomer(keyword) {
  const page = await http.get('/admin/customer/page', { params: { page: 1, size: 20, keyword: keyword || undefined } })
  customers.value = page.records || []
}

function openCreate() {
  form.value = { title: '', customerId: null, amount: '', settleDate: today(), remark: '' }
  dialog.value = true
  searchCustomer('')
}

async function save() {
  const amount = Number(form.value.amount)
  if (!form.value.title?.trim()) return ElMessage.warning('请填写结算名称')
  if (!(amount > 0)) return ElMessage.warning('请填写金额')
  if (!form.value.settleDate) return ElMessage.warning('请选择结算日')
  await http.post('/admin/finance/settlement', { ...form.value, amount, customerId: form.value.customerId || null })
  ElMessage.success('已入账')
  dialog.value = false
  load()
}

async function voidRow(row) {
  await ElMessageBox.confirm(`作废结算「${row.title}」？金额不再计入已确认收入。`, '作废')
  await http.post(`/admin/finance/settlement/${row.id}/void`)
  ElMessage.success('已作废')
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
