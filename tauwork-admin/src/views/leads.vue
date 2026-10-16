<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;gap:10px;align-items:center">
        <b>线索池</b>
        <el-select v-model="q.status" placeholder="全部状态" clearable style="width:130px;margin-left:auto" @change="load">
          <el-option v-for="(t, s) in statusMap" :key="s" :label="t" :value="s" />
        </el-select>
        <el-button @click="load">刷新</el-button>
        <el-button type="warning" plain @click="recycle">手动回收72h</el-button>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="线索ID" width="170" show-overflow-tooltip />
      <el-table-column label="来源" width="110">
        <template #default="{ row }">{{ sourceMap[row.source] || row.source }}</template>
      </el-table-column>
      <el-table-column label="评估分" width="80">
        <template #default="{ row }">
          <b v-if="row.assessScore" style="color:#C2A05F">{{ row.assessScore }}</b>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="intent" label="业务意向" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)" size="small" effect="dark">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="归属顾问ID" width="120">
        <template #default="{ row }">{{ row.ownerId || '—' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170">
        <template #default="{ row }">{{ (row.createTime || '').slice(0, 16) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'NEW'" size="small" type="primary"
                     @click="assign(row)">分配</el-button>
          <el-button v-if="row.status === 'PUBLIC_POOL'" size="small" type="success" plain
                     @click="claim(row)">领取</el-button>
          <el-button v-if="['ASSIGNED', 'IN_PROGRESS'].includes(row.status)" size="small"
                     @click="followUp(row)">跟进打卡</el-button>
          <el-button v-if="['ASSIGNED', 'IN_PROGRESS'].includes(row.status)" size="small" type="warning" plain
                     @click="release(row)">退回公海</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const list = ref([])
const q = reactive({ status: null })

const statusMap = { NEW: '新线索', ASSIGNED: '已分配', IN_PROGRESS: '跟进中', CONVERTED: '已成交', PUBLIC_POOL: '公海池' }
const sourceMap = { ASSESS: '智能评估', BOOKING: '咨询预约', CONTENT: '内容留资', REFERRAL: '转介绍', EVENT: '沙龙活动', RENEWAL_CAL: '续签日历' }

async function load() {
  const page = await http.get('/admin/lead/page', { params: { page: 1, size: 50, status: q.status || undefined } })
  list.value = page.records
}

async function assign(row) {
  const { value } = await ElMessageBox.prompt('输入归属顾问ID（sys_user.id）', '分配线索', {
    inputPattern: /^\d+$/, inputErrorMessage: '请输入数字ID'
  }).catch(() => ({ value: null }))
  if (!value) return
  await http.put(`/admin/lead/${row.id}/assign/${value}`)
  ElMessage.success('已分配')
  load()
}

async function claim(row) {
  await http.put(`/admin/lead/${row.id}/claim`)
  ElMessage.success('已领取到自己名下')
  load()
}

async function followUp(row) {
  await http.put(`/admin/lead/${row.id}/follow-up`)
  ElMessage.success('已打卡，72h计时刷新')
  load()
}

async function release(row) {
  await ElMessageBox.confirm('确认放弃跟进，退回公海池？', '操作确认', { type: 'warning' })
  await http.put(`/admin/lead/${row.id}/release`)
  ElMessage.success('已退回公海池')
  load()
}

async function recycle() {
  const n = await http.post('/admin/lead/recycle')
  ElMessage.success(n > 0 ? `已回收 ${n} 条超72小时未跟进线索` : '无待回收线索（72小时内均有跟进）')
  load()
}

const tagType = s => ({ NEW: 'info', ASSIGNED: 'primary', IN_PROGRESS: 'primary', CONVERTED: 'success', PUBLIC_POOL: 'warning' }[s])

onMounted(load)
</script>
