<template>
  <div>
    <div class="kpis">
      <div class="kpi"><span>服务订单</span><b>{{ list.length }}</b></div>
      <div class="kpi"><span>待支付</span><b>{{ count('PENDING_PAY') }}</b></div>
      <div class="kpi"><span>待确认到账</span><b>{{ count('CONFIRMING') }}</b></div>
      <div class="kpi"><span>已支付</span><b>{{ count('PAID') }}</b></div>
    </div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;gap:10px;align-items:center">
          <b>服务订单</b>
          <el-select v-model="status" placeholder="全部状态" clearable style="width:160px;margin-left:auto" @change="load">
            <el-option label="待支付" value="PENDING_PAY" />
            <el-option label="待确认到账" value="CONFIRMING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
          <el-button @click="load">刷新</el-button>
        </div>
      </template>
      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productName" label="服务" min-width="160" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column label="支付方式" width="110">
          <template #default="{ row }">{{ row.payChannel === 'TRANSFER' ? '对公转账' : '在线支付' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="凭证" width="90">
          <template #default="{ row }">
            <el-link v-if="row.voucherUrl" :href="row.voucherUrl" target="_blank" type="primary">查看</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 'CONFIRMING'" size="small" type="primary" @click="confirm(row)">确认到账</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const list = ref([])
const status = ref(null)

async function load() {
  list.value = await http.get('/admin/svc-order/list', { params: { status: status.value || undefined } })
}

function count(s) {
  return list.value.filter(item => item.status === s).length
}

function statusText(s) {
  return { PENDING_PAY: '待支付', CONFIRMING: '待确认到账', PAID: '已支付', CANCELED: '已取消' }[s] || s
}

async function confirm(row) {
  try {
    await ElMessageBox.confirm(`确认 ${row.orderNo} 已到账？`, '确认到账', { type: 'warning' })
    await http.put(`/admin/svc-order/${row.id}/confirm`)
    ElMessage.success('已确认到账')
  } catch (e) {
    if (e !== 'cancel' && e?.message && e.message !== 'cancel') ElMessage.error(e.message)
  }
  load()
}

onMounted(load)
</script>
