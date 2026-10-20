<template>
  <div>
    <div class="kpis">
      <div class="kpi"><span>定金持有</span><b>¥ {{ money(held('DEPOSIT')) }}</b></div>
      <div class="kpi"><span>押金持有</span><b>¥ {{ money(held('PLEDGE')) }}</b></div>
      <div class="kpi"><span>已退还</span><b>{{ rows.filter(r => r.status === 'REFUNDED').length }}</b></div>
      <div class="kpi"><span>已抵扣</span><b>{{ rows.filter(r => r.status === 'APPLIED').length }}</b></div>
    </div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px">
          <b>定金与押金</b>
          <el-select v-model="kind" clearable placeholder="全部类型" style="width:120px;margin-left:auto" @change="load">
            <el-option label="定金" value="DEPOSIT" />
            <el-option label="押金" value="PLEDGE" />
          </el-select>
          <el-select v-model="status" clearable placeholder="全部状态" style="width:120px" @change="load">
            <el-option label="持有" value="HELD" />
            <el-option label="已退还" value="REFUNDED" />
            <el-option label="已抵扣" value="APPLIED" />
          </el-select>
          <el-button type="primary" @click="openCreate">登记收取</el-button>
        </div>
      </template>
      <el-table :data="rows" v-loading="loading" stripe>
        <el-table-column label="客户" min-width="160">
          <template #default="{ row }">{{ row.customerName || '未命名' }}<div style="color:#8A94A8;font-size:12px">{{ row.customerPhone || '未留手机' }}</div></template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ row.kind === 'PLEDGE' ? '押金' : '定金' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">¥ {{ money(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" min-width="160" show-overflow-tooltip />
        <el-table-column label="登记时间" width="160">
          <template #default="{ row }">{{ time(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'HELD'" size="small" link type="primary" @click="move(row, 'refund', '确认退还？')">退还</el-button>
            <el-button v-if="row.status === 'HELD'" size="small" link type="success" @click="move(row, 'apply', '确认抵扣到费用？')">抵扣</el-button>
            <el-button v-if="row.status === 'HELD'" size="small" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog" title="登记收取" width="520px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="客户" required>
          <el-select v-model="form.customerId" filterable remote reserve-keyword :remote-method="searchCustomer" placeholder="搜姓名或手机号" style="width:100%">
            <el-option v-for="item in customers" :key="item.id" :label="labelOf(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="form.kind">
            <el-radio value="DEPOSIT">定金</el-radio>
            <el-radio value="PLEDGE">押金</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" required>
          <el-input v-model="form.amount" placeholder="0.00" />
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
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const rows = ref([])
const customers = ref([])
const loading = ref(false)
const dialog = ref(false)
const form = ref({})
const kind = ref('')
const status = ref('')

function money(v) { return Number(v || 0).toFixed(2) }
function time(v) { return v ? String(v).replace('T', ' ').slice(0, 16) : '—' }
function labelOf(item) { return `${item.name || '未命名'} ${item.phone || ''}`.trim() }
const statusText = s => ({ HELD: '持有', REFUNDED: '已退还', APPLIED: '已抵扣' }[s] || s)
const statusType = s => ({ HELD: 'warning', REFUNDED: 'info', APPLIED: 'success' }[s])
const held = k => rows.value.filter(r => r.kind === k && r.status === 'HELD').reduce((sum, r) => sum + Number(r.amount || 0), 0)

async function load() {
  loading.value = true
  try {
    rows.value = await http.get('/admin/finance/deposit/list', { params: { kind: kind.value || undefined, status: status.value || undefined } })
  } finally { loading.value = false }
}

async function searchCustomer(keyword) {
  const page = await http.get('/admin/customer/page', { params: { page: 1, size: 20, keyword: keyword || undefined } })
  customers.value = page.records || []
}

function openCreate() {
  form.value = { customerId: null, kind: 'DEPOSIT', amount: '', remark: '' }
  dialog.value = true
  searchCustomer('')
}

async function save() {
  const amount = Number(form.value.amount)
  if (!form.value.customerId) return ElMessage.warning('请选择客户')
  if (!(amount > 0)) return ElMessage.warning('请填写金额')
  await http.post('/admin/finance/deposit', { ...form.value, amount })
  ElMessage.success('已登记')
  dialog.value = false
  load()
}

async function move(row, action, text) {
  await ElMessageBox.confirm(text, '处理')
  await http.post(`/admin/finance/deposit/${row.id}/${action}`)
  ElMessage.success('已更新')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm('删除这条收取记录？', '删除')
  await http.delete(`/admin/finance/deposit/${row.id}`)
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
