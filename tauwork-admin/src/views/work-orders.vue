<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;align-items:center;gap:12px">
        <b>工单管理</b>
        <span style="color:#8A94A8;font-size:12px">通用流程：待处理、处理中、已完成</span>
        <el-select v-model="status" clearable placeholder="全部状态" style="width:140px;margin-left:auto" @change="load">
          <el-option v-for="item in statuses" :key="item" :label="statusText(item)" :value="item" />
        </el-select>
        <el-button type="primary" @click="openCreate">新建工单</el-button>
      </div>
    </template>
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="客户" min-width="150">
        <template #default="{ row }">
          <span v-if="row.customerId">{{ row.customerName || '未命名' }}<div style="color:#8A94A8;font-size:12px">{{ row.customerPhone || '未留手机' }}</div></span>
          <span v-else style="color:#C0C4CC">未关联</span>
        </template>
      </el-table-column>
      <el-table-column label="处理人" width="120">
        <template #default="{ row }">{{ row.assigneeName || '未指派' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="content" label="处理要求" min-width="180" show-overflow-tooltip />
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">{{ time(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openDetail(row)">处理</el-button>
          <el-button v-if="row.status === 'OPEN' || row.status === 'PROCESSING'" size="small" link @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'OPEN' || row.status === 'CANCELED'" size="small" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑工单' : '新建工单'" width="560px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="128" />
        </el-form-item>
        <el-form-item label="客户">
          <el-select v-model="form.customerId" filterable remote clearable reserve-keyword :remote-method="searchCustomer"
                     placeholder="搜姓名或手机号，可空" style="width:100%">
            <el-option v-for="item in customers" :key="item.id" :label="labelOf(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人">
          <el-select v-model="form.assigneeId" clearable filterable placeholder="未指派" style="width:100%">
            <el-option v-for="item in staff" :key="item.id" :label="item.realName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理要求">
          <el-input v-model="form.content" type="textarea" :rows="3" maxlength="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawer" title="工单处理" size="420px">
      <template v-if="current">
        <div style="margin-bottom:12px">
          <b>{{ current.title }}</b>
          <el-tag :type="statusType(current.status)" size="small" style="margin-left:8px">{{ statusText(current.status) }}</el-tag>
        </div>
        <div style="color:#8A94A8;font-size:13px;margin-bottom:12px">
          {{ current.customerName || '未关联客户' }} · {{ current.assigneeName || '未指派' }}
        </div>
        <div v-if="current.content" style="margin-bottom:16px;white-space:pre-wrap">{{ current.content }}</div>
        <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:16px">
          <el-button v-if="current.status === 'OPEN'" type="primary" @click="act('start')">开始处理</el-button>
          <el-button v-if="current.status === 'PROCESSING'" type="success" @click="finish">完成</el-button>
          <el-button v-if="current.status === 'OPEN' || current.status === 'PROCESSING'" @click="cancel">取消工单</el-button>
          <el-button @click="note">追加说明</el-button>
        </div>
        <el-timeline>
          <el-timeline-item v-for="item in current.logs || []" :key="item.id" :timestamp="time(item.createTime)">
            <b>{{ actionText(item.action) }}</b>
            <span style="color:#8A94A8"> {{ item.operatorName || '' }}</span>
            <div v-if="item.content">{{ item.content }}</div>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const rows = ref([])
const customers = ref([])
const staff = ref([])
const loading = ref(false)
const dialog = ref(false)
const drawer = ref(false)
const form = ref({})
const current = ref(null)
const status = ref('')
const statuses = ['OPEN', 'PROCESSING', 'DONE', 'CANCELED']

const statusText = s => ({ OPEN: '待处理', PROCESSING: '处理中', DONE: '已完成', CANCELED: '已取消' }[s] || s)
const statusType = s => ({ OPEN: 'warning', PROCESSING: 'primary', DONE: 'success', CANCELED: 'info' }[s])
const actionText = s => ({ CREATE: '新建', START: '开始处理', FINISH: '完成', CANCEL: '取消', NOTE: '说明' }[s] || s)
function time(v) { return v ? String(v).replace('T', ' ').slice(0, 16) : '—' }
function labelOf(item) { return `${item.name || '未命名'} ${item.phone || ''}`.trim() }

async function load() {
  loading.value = true
  try { rows.value = await http.get('/admin/work-order/list', { params: { status: status.value || undefined } }) }
  finally { loading.value = false }
}

async function searchCustomer(keyword) {
  const page = await http.get('/admin/customer/page', { params: { page: 1, size: 20, keyword: keyword || undefined } })
  const found = page.records || []
  const currentCustomer = customers.value.find(item => item.id === form.value.customerId)
  customers.value = currentCustomer && !found.some(item => item.id === currentCustomer.id) ? [currentCustomer, ...found] : found
}

function openCreate() {
  form.value = { title: '', customerId: null, assigneeId: null, content: '' }
  customers.value = []
  dialog.value = true
  searchCustomer('')
}

function openEdit(row) {
  form.value = { id: row.id, title: row.title, customerId: row.customerId, assigneeId: row.assigneeId, content: row.content || '' }
  customers.value = row.customerId ? [{ id: row.customerId, name: row.customerName, phone: row.customerPhone }] : []
  dialog.value = true
}

async function save() {
  if (!form.value.title?.trim()) return ElMessage.warning('请填写工单标题')
  if (form.value.id) await http.put('/admin/work-order', form.value)
  else await http.post('/admin/work-order', form.value)
  ElMessage.success('已保存')
  dialog.value = false
  load()
}

async function openDetail(row) {
  current.value = await http.get(`/admin/work-order/${row.id}`)
  drawer.value = true
}

async function act(name, body) {
  current.value = await http.post(`/admin/work-order/${current.value.id}/${name}`, body || {})
  ElMessage.success('已更新')
  load()
}

async function finish() {
  try {
    const { value } = await ElMessageBox.prompt('填写处理结果', '完成工单', { inputPlaceholder: '做了什么' })
    await act('finish', { content: value })
  } catch (e) { quiet(e) }
}

async function cancel() {
  try {
    const { value } = await ElMessageBox.prompt('取消原因，可留空', '取消工单', { inputPlaceholder: '选填', inputValidator: () => true })
    await act('cancel', { content: value })
  } catch (e) { quiet(e) }
}

async function note() {
  try {
    const { value } = await ElMessageBox.prompt('处理说明', '追加说明')
    await act('note', { content: value })
  } catch (e) { quiet(e) }
}

function quiet(e) {
  if (e === 'cancel' || e?.message === 'cancel') return
  if (e?.message) ElMessage.error(e.message)
}

async function remove(row) {
  await ElMessageBox.confirm(`删除工单「${row.title}」？`, '删除工单')
  await http.delete(`/admin/work-order/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  staff.value = await http.get('/admin/staff')
  load()
})
</script>
