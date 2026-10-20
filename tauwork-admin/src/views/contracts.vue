<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;align-items:center;gap:12px">
        <b>合同订单</b>
        <span style="color:#8A94A8;font-size:12px">登记合同金额、客户，并上传合同附件</span>
        <el-button style="margin-left:auto" type="primary" @click="openCreate">新建合同</el-button>
      </div>
    </template>
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column label="客户" min-width="160">
        <template #default="{ row }">{{ row.customerName || '未命名' }}<div style="color:#8A94A8;font-size:12px">{{ row.customerPhone || '未留手机' }}</div></template>
      </el-table-column>
      <el-table-column label="合同金额" width="140">
        <template #default="{ row }">¥ {{ money(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="合同附件" min-width="200">
        <template #default="{ row }">
          <a v-if="row.fileUrl" :href="row.fileUrl" target="_blank">{{ row.fileName || '查看附件' }}</a>
          <span v-else style="color:#C0C4CC">未上传</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="登记时间" width="170">
        <template #default="{ row }">{{ time(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑合同' : '新建合同'" width="560px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="客户" required>
          <el-select v-model="form.customerId" filterable remote reserve-keyword :remote-method="searchCustomer"
                     placeholder="搜姓名或手机号" style="width:100%">
            <el-option v-for="item in customers" :key="item.id" :label="labelOf(item)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同金额" required>
          <el-input v-model="form.amount" placeholder="0.00" />
        </el-form-item>
        <el-form-item label="合同附件">
          <div style="display:flex;gap:12px;align-items:center">
            <a v-if="form.fileUrl" :href="form.fileUrl" target="_blank">{{ form.fileName || '已上传' }}</a>
            <el-upload :action="'/api/admin/file/upload'" :headers="uploadHeaders" :show-file-list="false"
                       accept=".pdf,.jpg,.jpeg,.png,.webp" :on-success="onUploaded" :on-error="() => ElMessage.error('上传失败')">
              <el-button>上传附件</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" maxlength="255" placeholder="合同说明，选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
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
const uploadHeaders = computed(() => ({ Authorization: localStorage.getItem('token') || '' }))

function money(v) { return Number(v || 0).toFixed(2) }
function time(v) { return v ? String(v).replace('T', ' ').slice(0, 16) : '—' }
function labelOf(item) { return `${item.name || '未命名'} ${item.phone || ''}`.trim() }

async function load() {
  loading.value = true
  try { rows.value = await http.get('/admin/contract/list') } finally { loading.value = false }
}

async function searchCustomer(keyword) {
  const page = await http.get('/admin/customer/page', { params: { page: 1, size: 20, keyword: keyword || undefined } })
  const found = page.records || []
  const current = customers.value.find(item => item.id === form.value.customerId)
  customers.value = current && !found.some(item => item.id === current.id) ? [current, ...found] : found
}

function openCreate() {
  form.value = { id: null, customerId: null, amount: '', fileUrl: '', fileName: '', remark: '' }
  customers.value = []
  dialog.value = true
  searchCustomer('')
}

function openEdit(row) {
  form.value = { ...row, amount: money(row.amount) }
  customers.value = [{ id: row.customerId, name: row.customerName, phone: row.customerPhone }]
  dialog.value = true
}

function onUploaded(res) {
  if (res.code !== 0) return ElMessage.error(res.msg || '上传失败')
  form.value.fileUrl = res.data.url
  form.value.fileName = res.data.originalName || '合同附件'
}

async function save() {
  const amount = Number(form.value.amount)
  if (!form.value.customerId) return ElMessage.warning('请选择客户')
  if (!(amount >= 0) || Number.isNaN(amount)) return ElMessage.warning('请填写合同金额')
  const payload = { ...form.value, amount }
  if (payload.id) await http.put('/admin/contract', payload)
  else await http.post('/admin/contract', payload)
  ElMessage.success('已保存')
  dialog.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm('删除这份合同记录？附件文件仍保留在服务器上。', '删除合同')
  await http.delete(`/admin/contract/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
