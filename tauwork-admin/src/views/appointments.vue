<template>
  <div>
    <div class="kpis">
      <div class="kpi"><span>今日待确认</span><b>{{ todayPending }}</b></div>
      <div class="kpi"><span>今日已确认</span><b>{{ todayConfirmed }}</b></div>
      <div class="kpi"><span>本页预约</span><b>{{ rows.length }}</b></div>
    </div>
    <div class="panel">
      <div class="toolbar">
        <el-date-picker v-model="q.bookDate" type="date" value-format="YYYY-MM-DD" placeholder="预约日期" clearable style="width:150px" />
        <el-select v-model="q.status" placeholder="全部状态" clearable style="width:130px">
          <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="key" />
        </el-select>
        <el-input v-model="q.keyword" placeholder="客户 / 手机 / 主题" clearable style="width:220px" @keyup.enter="load" />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button v-if="canBook" @click="openCreate">登记预约</el-button>
      </div>
      <el-table :data="rows" v-loading="loading">
        <el-table-column label="客户" width="150">
          <template #default="{ row }">
            <b>{{ row.customerName || '未绑定' }}</b>
            <div class="sub">{{ row.customerPhone || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="服务项目" min-width="160">
          <template #default="{ row }">
            <div>{{ row.productName || '未指定项目' }}</div>
            <div class="sub">{{ row.topic }}</div>
          </template>
        </el-table-column>
        <el-table-column label="方式" width="120">
          <template #default="{ row }">{{ modeMap[row.mode] || row.mode }}</template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ row.bookDate }} {{ row.timeSlot }}</template>
        </el-table-column>
        <el-table-column label="顾问" width="110">
          <template #default="{ row }">{{ row.consultantName || '待分配' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="tagType[row.status]" effect="plain">{{ statusMap[row.status] }}</el-tag></template>
        </el-table-column>
        <el-table-column label="备注" min-width="140" prop="remark" show-overflow-tooltip />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <template v-if="canBook">
              <el-button v-if="row.status === 'PENDING'" link type="primary" @click="change(row, 'CONFIRMED')">确认</el-button>
              <el-button v-if="row.status === 'CONFIRMED'" link type="success" @click="change(row, 'DONE')">完成</el-button>
              <el-button v-if="row.status === 'CONFIRMED'" link type="warning" @click="change(row, 'NO_SHOW')">爽约</el-button>
              <el-button v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'" link type="danger" @click="cancel(row)">取消</el-button>
            </template>
            <span v-else class="sub">—</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" title="登记咨询预约" width="560px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="手机号" required>
          <div class="phone-row">
            <el-input v-model="form.phone" maxlength="11" placeholder="客户手机号" />
            <el-button @click="lookup">查找</el-button>
          </div>
          <div class="sub">{{ lookupText }}</div>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="新客户需要填写姓名" />
        </el-form-item>
        <el-form-item label="服务项目">
          <el-select v-model="form.productId" filterable clearable placeholder="选择服务项目" style="width:100%" @change="pickProduct">
            <el-option v-for="item in products" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="咨询方式" required>
          <el-radio-group v-model="form.mode">
            <el-radio-button v-for="(label, key) in modeMap" :key="key" :value="key">{{ label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="主题" required>
          <el-input v-model="form.topic" maxlength="128" placeholder="咨询主题" />
        </el-form-item>
        <el-form-item label="日期" required>
          <el-date-picker v-model="form.bookDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="时段" required>
          <div class="slots">
            <button v-for="slot in slots" :key="slot" type="button" :class="{ on: form.timeSlot === slot }" @click="form.timeSlot = slot">{{ slot }}</button>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const statusMap = { PENDING: '待确认', CONFIRMED: '已确认', DONE: '已完成', CANCELED: '已取消', NO_SHOW: '爽约' }
const tagType = { PENDING: 'warning', CONFIRMED: 'primary', DONE: 'success', CANCELED: 'info', NO_SHOW: 'danger' }
const modeMap = { VIDEO: '视频咨询', FUTIAN: '深圳·福田', BAOAN: '深圳·宝安', KWAI_TSING: '香港·葵青' }
const slots = ['09:30', '10:30', '14:00', '15:30', '16:30', '19:00']
const user = JSON.parse(localStorage.getItem('user') || '{}')
const canBook = (user.perms || []).includes('*') || (user.perms || []).includes('booking:manage')

const q = reactive({ bookDate: '', status: '', keyword: '' })
const rows = ref([])
const loading = ref(false)
const today = new Date().toISOString().slice(0, 10)
const todayPending = computed(() => rows.value.filter(r => r.bookDate === today && r.status === 'PENDING').length)
const todayConfirmed = computed(() => rows.value.filter(r => r.bookDate === today && r.status === 'CONFIRMED').length)

const visible = ref(false)
const products = ref([])
const lookupText = ref('先查找手机号，没有档案时会按姓名新建')
const form = reactive({
  phone: '', name: '', customerId: null, createIfAbsent: true,
  productId: null, mode: 'VIDEO', topic: '', bookDate: today, timeSlot: '10:30', remark: ''
})

async function load() {
  loading.value = true
  try {
    rows.value = await http.get('/admin/svc-booking/list', { params: q })
  } finally { loading.value = false }
}

async function change(row, status) {
  await http.put(`/admin/svc-booking/${row.id}/status/${status}`)
  ElMessage.success('已更新')
  load()
}

async function cancel(row) {
  const { value } = await ElMessageBox.prompt('取消原因', '取消预约', { inputPlaceholder: '选填' })
  await http.put(`/admin/svc-booking/${row.id}/status/CANCELED`, null, { params: { failReason: value || '' } })
  ElMessage.success('已取消')
  load()
}

async function openCreate() {
  Object.assign(form, {
    phone: '', name: '', customerId: null, createIfAbsent: true,
    productId: null, mode: 'VIDEO', topic: '', bookDate: today, timeSlot: '10:30', remark: ''
  })
  lookupText.value = '先查找手机号，没有档案时会按姓名新建'
  if (!products.value.length) {
    const page = await http.get('/admin/product/page', { params: { prodType: 'SERVICE', onSale: 1, size: 100 } })
    products.value = page.records || []
  }
  visible.value = true
}

function pickProduct(id) {
  const found = products.value.find(item => item.id === id)
  if (found && !form.topic) form.topic = found.name
}

async function lookup() {
  if (!/^1\d{10}$/.test(form.phone)) {
    ElMessage.warning('请输入 11 位手机号')
    return
  }
  const found = await http.get('/admin/customer/by-phone', { params: { phone: form.phone } })
  if (!found) {
    form.customerId = null
    lookupText.value = '还没有档案，保存时会新建客户'
    return
  }
  form.customerId = found.id
  form.name = found.name || form.name
  lookupText.value = `已找到 ${found.name || '未命名'}，预约会记到这份档案`
}

async function save() {
  if (!/^1\d{10}$/.test(form.phone)) return ElMessage.warning('请输入 11 位手机号')
  if (!form.topic.trim()) return ElMessage.warning('请填写咨询主题')
  if (!form.bookDate || !form.timeSlot) return ElMessage.warning('请选择日期和时段')
  await http.post('/admin/svc-booking', { ...form, topic: form.topic.trim() })
  ElMessage.success('预约已登记')
  visible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.kpis { display: flex; gap: 14px; margin-bottom: 14px; }
.kpi { flex: 1; background: #fff; border-radius: 12px; padding: 16px 18px; }
.kpi span { color: var(--abos-muted); font-size: 12px; }
.kpi b { display: block; margin-top: 6px; font-size: 26px; }
.sub { color: var(--abos-muted); font-size: 12px; }
.phone-row { display: flex; gap: 8px; width: 100%; }
.slots { display: flex; flex-wrap: wrap; gap: 8px; }
.slots button { border: 1px solid var(--el-border-color); background: #fff; border-radius: 8px; padding: 6px 12px; cursor: pointer; }
.slots button.on { background: var(--abos-gold); color: #fff; border-color: var(--abos-gold); }
</style>
