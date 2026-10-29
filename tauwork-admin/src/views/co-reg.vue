<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;gap:10px;align-items:center">
          <b>公司注册工单</b>
          <el-select v-model="step" placeholder="全部步骤" clearable style="width:160px;margin-left:auto" @change="load">
            <el-option v-for="s in steps" :key="s.v" :label="s.label" :value="s.v" />
          </el-select>
          <el-button @click="load">刷新</el-button>
        </div>
      </template>
      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="客户" width="140">
          <template #default="{ row }">{{ row.customerName || '—' }}<div style="color:#909399">{{ row.customerPhone }}</div></template>
        </el-table-column>
        <el-table-column prop="companyName" label="公司名称" min-width="160" />
        <el-table-column prop="stepLabel" label="当前步骤" width="120" />
        <el-table-column prop="stepStatusLabel" label="状态" width="120" />
        <el-table-column label="支付" width="110">
          <template #default="{ row }">{{ payText(row.payStatus) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="建立时间" width="170" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }"><el-button size="small" @click="open(row)">核对</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="show" title="公司注册工单" size="560px">
      <template v-if="cur">
        <p><b>{{ cur.displayName }}</b> · {{ cur.productName }}</p>
        <p>订单 {{ cur.orderNo }} · {{ payText(cur.payStatus) }}<span v-if="cur.amount"> · ¥{{ cur.amount }}</span></p>
        <p>客户 {{ cur.customerName || '—' }} {{ cur.customerPhone || '' }}</p>
        <p>进度：{{ cur.stepLabel }} · {{ cur.stepStatusLabel }}</p>
        <el-alert v-if="cur.remark" :title="cur.remark" type="warning" :closable="false" style="margin:12px 0" />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="中文名称">{{ cur.companyName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="英文名称">{{ cur.companyNameEn || '—' }}</el-descriptions-item>
          <el-descriptions-item label="主营业务">{{ cur.bizScope || '—' }}</el-descriptions-item>
          <el-descriptions-item label="注册资金">{{ cur.capitalAmount }} 港币</el-descriptions-item>
        </el-descriptions>
        <el-table :data="cur.members || []" size="small" style="margin-top:16px">
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column label="身份" width="110">
            <template #default="{ row }">{{ roleText(row.role) }}</template>
          </el-table-column>
          <el-table-column label="持股" width="80">
            <template #default="{ row }">{{ row.role === 'DIRECTOR' ? '—' : row.shareRatio + '%' }}</template>
          </el-table-column>
          <el-table-column label="证件">
            <template #default="{ row }">
              <el-image v-if="row.idFrontUrl" :src="row.idFrontUrl" :preview-src-list="photos(row)" style="width:48px;height:48px;margin-right:6px" fit="cover" />
              <el-image v-if="row.idBackUrl" :src="row.idBackUrl" :preview-src-list="photos(row)" style="width:48px;height:48px;margin-right:6px" fit="cover" />
              <el-image v-if="row.idHoldUrl" :src="row.idHoldUrl" :preview-src-list="photos(row)" style="width:48px;height:48px" fit="cover" />
            </template>
          </el-table-column>
        </el-table>
        <el-input v-model="remark" type="textarea" :rows="3" placeholder="核对说明。退回时必填" style="margin-top:16px" />
        <div style="margin-top:12px;display:flex;gap:8px">
          <el-button v-if="canReview" type="primary" @click="review(true)">核对通过</el-button>
          <el-button v-if="canReview" @click="review(false)">退回修改</el-button>
          <el-button v-if="cur.step === 'SIGN' && cur.stepStatus === 'WAIT_OPS'" type="primary" @click="review(true)">文件已备妥</el-button>
          <el-button v-if="cur.step === 'FILING'" type="primary" @click="review(true)">已递交注册处</el-button>
          <el-button v-if="cur.step === 'CERT'" type="primary" @click="review(true)">证书已领取</el-button>
        </div>
        <el-timeline style="margin-top:20px">
          <el-timeline-item v-for="log in cur.logs || []" :key="log.id" :timestamp="log.createTime">
            {{ actionText(log.action) }} {{ log.remark || '' }}
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../utils/http'

const list = ref([])
const step = ref(null)
const show = ref(false)
const cur = ref(null)
const remark = ref('')
const steps = [
  { v: 'NAME', label: '公司核名' },
  { v: 'PROFILE', label: '公司资料' },
  { v: 'KYC', label: '身份资料' },
  { v: 'SIGN', label: '签署文件' },
  { v: 'FILING', label: '递交注册处' },
  { v: 'CERT', label: '领取证书' },
  { v: 'DONE', label: '完成交付' }
]

const canReview = ref(false)

async function load() {
  list.value = await http.get('/admin/co-reg/list', { params: { step: step.value || undefined } })
}

async function open(row) {
  cur.value = await http.get(`/admin/co-reg/${row.id}`)
  canReview.value = ['NAME', 'PROFILE', 'KYC'].includes(cur.value.step) && cur.value.stepStatus === 'WAIT_REVIEW'
  remark.value = ''
  show.value = true
}

function roleText(role) {
  return { SHAREHOLDER: '股东', DIRECTOR: '董事', BOTH: '股东兼董事' }[role] || role
}

function payText(status) {
  return { PENDING_PAY: '待支付', CONFIRMING: '待确认到账', PAID: '已支付', CANCELED: '已取消' }[status] || status || '—'
}

function photos(row) {
  return [row.idFrontUrl, row.idBackUrl, row.idHoldUrl].filter(Boolean)
}

function actionText(action) {
  return { OPEN: '建立工单', SUBMIT: '客户提交', PASS: '核对通过', REJECT: '退回', READY: '文件备妥', SIGN: '确认签署', CANCEL: '取消' }[action] || action
}

async function review(pass) {
  try {
    cur.value = await http.post(`/admin/co-reg/${cur.value.id}/review`, { pass, remark: remark.value })
    canReview.value = ['NAME', 'PROFILE', 'KYC'].includes(cur.value.step) && cur.value.stepStatus === 'WAIT_REVIEW'
    remark.value = ''
    ElMessage.success('已更新')
    load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(load)
</script>
