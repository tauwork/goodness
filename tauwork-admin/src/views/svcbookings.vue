<template>
  <div>
    <div class="kpis">
      <div class="kpi"><span>待确认</span><b style="color:#C2A05F">{{ count('PENDING') }}</b></div>
      <div class="kpi"><span>已确认待完成</span><b style="color:#16355E">{{ count('CONFIRMED') }}</b></div>
      <div class="kpi"><span>已完成</span><b style="color:#2FA36B">{{ count('DONE') }}</b></div>
      <div class="kpi"><span>已取消/爽约</span><b style="color:#D9534F">{{ count('CANCELED') + count('NO_SHOW') }}</b></div>
    </div>

    <el-card shadow="never">
      <template #header>
        <div style="display:flex;gap:10px;align-items:center">
          <b>服务咨询预约</b>
          <el-select v-model="q.status" placeholder="全部状态" clearable style="width:130px;margin-left:auto" @change="load">
            <el-option v-for="(t, s) in statusMap" :key="s" :label="t" :value="s" />
          </el-select>
          <el-button @click="load">刷新</el-button>
        </div>
      </template>
      <el-table :data="list" stripe>
        <el-table-column prop="bookDate" label="预约日期" width="110" />
        <el-table-column prop="timeSlot" label="时段" width="80" />
        <el-table-column prop="topic" label="咨询主题" min-width="180" show-overflow-tooltip />
        <el-table-column label="方式" width="110">
          <template #default="{ row }">{{ modeText(row.mode) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="客户补充" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)" size="small" effect="dark">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="归因" width="100" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="primary"
                       @click="change(row, 'CONFIRMED')">确认</el-button>
            <el-button v-if="row.status === 'CONFIRMED'" size="small" type="success"
                       @click="change(row, 'DONE')">完成</el-button>
            <el-button v-if="['PENDING', 'CONFIRMED'].includes(row.status)" size="small" type="danger" plain
                       @click="cancel(row)">取消</el-button>
            <el-button v-if="row.status === 'CONFIRMED'" size="small" type="warning" plain
                       @click="noShow(row)">爽约</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const list = ref([])
const q = reactive({ status: null })

const statusMap = {
  PENDING: '待确认', CONFIRMED: '已确认', DONE: '已完成', CANCELED: '已取消', NO_SHOW: '爽约'
}
const failOptions = ['价格/预算', '暂缓决策', '资格不足', '竞对比价', '其他']

async function load() {
  list.value = await http.get('/admin/svc-booking/list', { params: { status: q.status || undefined } })
}

async function change(row, status) {
  await http.put(`/admin/svc-booking/${row.id}/status/${status}`)
  ElMessage.success(status === 'CONFIRMED' ? '已确认' : '已完成')
  load()
}

async function cancel(row) {
  const { value } = await ElMessageBox.prompt('填写取消归因（可选）', `取消「${row.topic}」`, {
    inputPlaceholder: '如：客户暂缓决策'
  }).catch(() => ({ value: null }))
  if (value === null) return
  await http.put(`/admin/svc-booking/${row.id}/status/CANCELED`, { params: { failReason: value || undefined } })
  ElMessage.success('已取消')
  load()
}

async function noShow(row) {
  await http.put(`/admin/svc-booking/${row.id}/status/NO_SHOW`)
  ElMessage.success('已标记爽约')
  load()
}

const count = s => list.value.filter(x => x.status === s).length
const modeText = m => ({ VIDEO: '视频', FUTIAN: '深圳·福田', BAOAN: '深圳·宝安', KWAI_TSING: '香港·葵青' }[m] || m)
const tagType = s => ({ PENDING: 'warning', CONFIRMED: 'primary', DONE: 'success', CANCELED: 'danger', NO_SHOW: 'danger' }[s])

onMounted(load)
</script>

<style scoped>
.kpis { display:grid; grid-template-columns:repeat(4,1fr); gap:14px; margin-bottom:16px; }
.kpi { background:#fff; border-radius:12px; padding:14px 18px; border:1px solid #E8ECF3; }
.kpi span { font-size:11.5px; color:#8A94A8; display:block; }
.kpi b { font-size:22px; color:#16355E; margin-top:6px; display:block; }
</style>
