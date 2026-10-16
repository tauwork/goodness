<template>
  <div>
    <el-card shadow="never">
      <template #header><b>经营驾驶舱</b><span style="color:#8A94A8;font-size:12px;margin-left:8px">实时数据（V0.4 首版看板，V0.5 后补趋势图）</span></template>
      <div class="grid">
        <div class="card" v-for="c in cards" :key="c.label">
          <span>{{ c.label }}</span><b :style="{color:c.color}">{{ c.value }}</b>
          <i>{{ c.desc }}</i>
        </div>
      </div>
    </el-card>
    <el-card shadow="never" style="margin-top:14px">
      <template #header>
        <div style="display:flex;align-items:center"><b>最新预订动态</b>
          <el-button style="margin-left:auto" size="small" @click="$router.push('/bookings')">查看全部</el-button></div>
      </template>
      <el-table :data="latest" stripe>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="amount" label="金额" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../utils/http'

const list = ref([])
onMounted(async () => { list.value = await http.get('/admin/booking/list') })

const latest = computed(() => list.value.slice(0, 8))
const cards = computed(() => [
  { label: '今日预订营收', value: '¥' + list.value.filter(x => ['PAID', 'USED'].includes(x.status)).reduce((s, x) => s + +x.amount, 0), desc: '已支付订单合计', color: '#16355E' },
  { label: '待使用订单', value: list.value.filter(x => x.status === 'PAID').length, desc: '客户已支付待到店', color: '#C2A05F' },
  { label: '累计核销', value: list.value.filter(x => x.status === 'USED').length, desc: '已到店使用', color: '#2FA36B' },
  { label: '退款订单', value: list.value.filter(x => x.status === 'CANCELED').length, desc: '已退订', color: '#D9534F' }
])
const statusText = s => ({ PENDING_PAY: '待支付', PAID: '已支付', USED: '已使用', CANCELED: '已退订' }[s] || s)
</script>

<style scoped>
.grid { display:grid; grid-template-columns:repeat(4,1fr); gap:14px; }
.card { background:#F4F6FA; border-radius:12px; padding:18px; }
.card span { font-size:11.5px; color:#8A94A8; display:block; }
.card b { font-size:26px; display:block; margin:8px 0 4px; }
.card i { font-style:normal; font-size:11px; color:#8A94A8; }
</style>
