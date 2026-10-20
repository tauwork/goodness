<template>
  <div>
    <div class="kpis">
      <div class="kpi"><span>商品订单</span><b>{{ list.length }}</b></div>
      <div class="kpi"><span>待支付</span><b style="color:#C2A05F">{{ count('PENDING_PAY') }}</b></div>
      <div class="kpi"><span>已支付待交付</span><b style="color:#16355E">{{ count('PAID') }}</b></div>
      <div class="kpi"><span>已交付</span><b style="color:#2FA36B">{{ count('DONE') }}</b></div>
      <div class="kpi"><span>已取消</span><b style="color:#D9534F">{{ count('CANCELED') }}</b></div>
    </div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;gap:10px;align-items:center">
          <b>商品订单</b>
          <el-select v-model="status" placeholder="全部状态" clearable style="width:140px;margin-left:auto" @change="load">
            <el-option label="待支付" value="PENDING_PAY" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已交付" value="DONE" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
          <el-button @click="load">刷新</el-button>
        </div>
      </template>
      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="170" />
        <el-table-column prop="productName" label="商品" min-width="160" />
        <el-table-column prop="spec" label="规格" width="90" />
        <el-table-column prop="qty" label="数量" width="70" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="更改状态" width="150">
          <template #default="{ row }">
            <el-select :model-value="row.status" size="small" style="width:120px" :disabled="row.status === 'CANCELED'" @change="next => change(row, next)">
              <el-option v-for="item in statusOptions" :key="item" :label="statusText(item)" :value="item" />
            </el-select>
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
  list.value = await http.get('/admin/goods-order/list', { params: { status: status.value || undefined } })
}

const statusOptions = ['PENDING_PAY', 'PAID', 'DONE', 'CANCELED']

async function change(row, next) {
  if (next === row.status) return
  const label = {
    PAID: '确认改为已支付？会计入收款',
    DONE: '确认改为已交付？',
    CANCELED: '确认取消？已支付订单会退款并退回库存'
  }[next] || '确认更改订单状态？'
  try {
    await ElMessageBox.confirm(label, '更改状态', { type: 'warning' })
    await http.put(`/admin/goods-order/${row.id}/status/${next}`)
    ElMessage.success('状态已更新')
  } catch (e) {
    if (e !== 'cancel' && e?.message && e.message !== 'cancel') ElMessage.error(e.message)
  }
  load()
}

const count = s => list.value.filter(x => x.status === s).length
const statusText = s => ({ PENDING_PAY: '待支付', PAID: '已支付', DONE: '已交付', CANCELED: '已取消' }[s] || s)
const statusType = s => ({ PENDING_PAY: 'warning', PAID: 'primary', DONE: 'success', CANCELED: 'info' }[s])

onMounted(load)
</script>

<style scoped>
.kpis { display:flex; gap:12px; margin-bottom:14px; }
.kpi { flex:1; background:#fff; border-radius:12px; padding:14px 16px; }
.kpi span { color:#8A94A8; font-size:12px; }
.kpi b { display:block; font-size:22px; margin-top:4px; }
</style>
