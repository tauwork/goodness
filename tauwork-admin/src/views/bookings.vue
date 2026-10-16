<template>
  <div>
    <div class="kpis">
      <div class="kpi"><span>预订订单（全部）</span><b>{{ list.length }}</b></div>
      <div class="kpi"><span>待支付</span><b style="color:#C2A05F">{{ count('PENDING_PAY') }}</b></div>
      <div class="kpi"><span>已支付待使用</span><b style="color:#16355E">{{ count('PAID') }}</b></div>
      <div class="kpi"><span>已使用</span><b style="color:#2FA36B">{{ count('USED') }}</b></div>
      <div class="kpi"><span>已退订</span><b style="color:#D9534F">{{ count('CANCELED') }}</b></div>
    </div>

    <el-card shadow="never">
      <template #header>
        <div style="display:flex;gap:10px;align-items:center">
          <b>空间预订订单</b>
          <el-select v-model="q.status" placeholder="全部状态" clearable style="width:140px;margin-left:auto" @change="load">
            <el-option label="待支付" value="PENDING_PAY" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已使用" value="USED" />
            <el-option label="已退订" value="CANCELED" />
          </el-select>
          <el-button @click="load">刷新</el-button>
        </div>
      </template>
      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="bookDate" label="预订日期" width="110" />
        <el-table-column label="时段" width="120">
          <template #default="{ row }">{{ row.startTime ? row.startTime + ' 起 ' + row.hours + '小时' : (row.qty ? 'x' + row.qty : '-') }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="entryCode" label="入场码" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.entryCode" type="warning" effect="plain">{{ row.entryCode }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="dark" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" min-width="180">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PAID'" size="small" type="success" @click="change(row, 'USED')">标记已使用</el-button>
            <el-button v-if="['PAID', 'PENDING_PAY'].includes(row.status)" size="small" type="danger" plain @click="change(row, 'CANCELED')">退订退款</el-button>
            <span v-if="row.status === 'USED'" style="color:#8A94A8;font-size:12px">核销于 {{ (row.usedAt || '').slice(0, 16) }}</span>
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

async function load() {
  list.value = await http.get('/admin/booking/list', { params: { status: q.status || undefined } })
}

async function change(row, status) {
  const label = status === 'USED' ? '确认客户已到店使用？将记录核销时间' : '确认退订？已支付订单将自动原路退款'
  await ElMessageBox.confirm(label, '操作确认', { type: 'warning' })
  await http.put(`/admin/booking/${row.id}/status/${status}`)
  ElMessage.success(status === 'USED' ? '已标记使用' : '已退订，退款已发起')
  load()
}

const count = s => list.value.filter(x => x.status === s).length
const statusText = s => ({ PENDING_PAY: '待支付', PAID: '已支付', USED: '已使用', CANCELED: '已退订' }[s] || s)
const statusType = s => ({ PENDING_PAY: 'warning', PAID: 'primary', USED: 'success', CANCELED: 'danger' }[s])

onMounted(load)
</script>

<style scoped>
.kpis { display:grid; grid-template-columns:repeat(5,1fr); gap:14px; margin-bottom:16px; }
.kpi { background:#fff; border-radius:12px; padding:14px 18px; border:1px solid #E8ECF3; }
.kpi span { font-size:11.5px; color:#8A94A8; display:block; }
.kpi b { font-size:22px; color:#16355E; margin-top:6px; display:block; }
</style>
