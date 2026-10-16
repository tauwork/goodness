<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;align-items:center"><b>产品管理</b>
        <el-radio-group v-model="type" style="margin-left:16px" @change="load">
          <el-radio-button value="SERVICE">服务产品</el-radio-button>
          <el-radio-button value="SPACE">空间产品</el-radio-button>
        </el-radio-group>
        <el-button style="margin-left:auto" @click="load">刷新</el-button>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="name" label="产品名称" min-width="200" />
      <el-table-column prop="subtitle" label="说明" min-width="240" show-overflow-tooltip />
      <el-table-column label="定价" width="140">
        <template #default="{ row }">
          <span v-if="row.priceValue">¥{{ row.priceValue }}</span>
          <el-tag v-else size="small" type="info">{{ priceModeText(row.priceMode) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="在售" width="80">
        <template #default="{ row }">
          <el-tag :type="row.onSale === 1 ? 'success' : 'info'" size="small">{{ row.onSale === 1 ? '在售' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.onSale === 1" size="small" plain type="warning" @click="offSale(row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../utils/http'

const list = ref([])
const type = ref('SERVICE')

async function load() {
  const page = await http.get('/admin/product/page', { params: { page: 1, size: 50 } })
  list.value = page.records.filter(p => !type.value || true)
  // 后端分页暂无 prodType 筛选，前端过滤
  list.value = page.records.filter(p => (type.value === 'SERVICE' ? p.prodType !== 'SPACE' : p.prodType === 'SPACE'))
}

async function offSale(row) {
  await http.put(`/admin/product/${row.id}/off-sale`)
  ElMessage.success('已下架')
  load()
}

const priceModeText = m => ({ QUOTE: '评估后报价', FIXED: '固定价', STAGED: '按阶段', MEMBER: '会员专享' }[m] || m)

onMounted(load)
</script>
