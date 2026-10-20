<template>
  <div class="panel">
    <div class="toolbar">
      <b>提成规则</b>
      <span class="hint">直接比例给成交客户的合伙人，间接比例给他的上级。商品和空间在支付成功时自动计提，服务项目在明细里登记成交。</span>
    </div>
    <el-table :data="rows" v-loading="loading">
      <el-table-column label="类型" width="120">
        <template #default="{ row }">{{ typeMap[row.prodType] || row.prodType }}</template>
      </el-table-column>
      <el-table-column prop="categoryName" label="类目" min-width="180" />
      <el-table-column label="直接提成" width="200">
        <template #default="{ row }">
          <el-input-number v-model="row.rate" :min="0" :max="100" :precision="2" :step="1" />
          <span class="unit">%</span>
        </template>
      </el-table-column>
      <el-table-column label="间接提成" width="200">
        <template #default="{ row }">
          <el-input-number v-model="row.rateL2" :min="0" :max="100" :precision="2" :step="1" />
          <span class="unit">%</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button link type="primary" @click="save(row)">保存</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../utils/http'

const typeMap = { SERVICE: '服务项目', SPACE: '空间产品', GOODS: '通用商品' }
const rows = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    rows.value = (await http.get('/admin/partner/rule/list')).map(row => ({
      ...row,
      rate: Number(row.rate || 0),
      rateL2: Number(row.rateL2 || 0)
    }))
  } finally { loading.value = false }
}

async function save(row) {
  await http.post('/admin/partner/rule', { categoryId: row.categoryId, rate: row.rate, rateL2: row.rateL2 })
  ElMessage.success('比例已保存')
}

onMounted(load)
</script>

<style scoped>
.hint { color: var(--abos-muted); font-size: 12px; margin-left: 12px; }
.unit { margin-left: 8px; color: var(--abos-muted); }
</style>
