<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;align-items:center"><b>社区店管理</b>
        <el-button style="margin-left:auto" @click="load">刷新</el-button></div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column prop="name" label="社区店" min-width="180" />
      <el-table-column prop="city" label="城市" width="90" />
      <el-table-column prop="address" label="地址" min-width="240" show-overflow-tooltip />
      <el-table-column prop="openHours" label="开放时间" min-width="160" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="店内产品" width="110">
        <template #default="{ row }">
          <el-button size="small" @click="showProducts(row)">查看 ({{ productCount[row.id] ?? '...' }})</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawer" :title="current?.name + ' · 可售空间产品'" size="640px">
      <el-table :data="bindings" stripe>
        <el-table-column prop="productId" label="产品ID" width="90" />
        <el-table-column label="社区价/单位" width="130">
          <template #default="{ row }">¥{{ row.price }} {{ row.priceUnit }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button size="small" type="danger" plain @click="unbind(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const list = ref([])
const productCount = reactive({})
const drawer = ref(false)
const current = ref(null)
const bindings = ref([])

async function load() {
  list.value = await http.get('/admin/community/list')
  list.value.forEach(async c => {
    const ps = await http.get(`/admin/community/${c.id}/product`)
    productCount[c.id] = ps.length
  })
}

async function showProducts(row) {
  current.value = row
  bindings.value = await http.get(`/admin/community/${row.id}/product`)
  drawer.value = true
}

async function unbind(row) {
  await ElMessageBox.confirm('确认从该社区店移除此产品？', '操作确认', { type: 'warning' })
  await http.delete(`/admin/community/bind-product/${row.id}`)
  ElMessage.success('已移除')
  showProducts(current.value)
  load()
}

const statusText = s => ({ OPEN: '营业中', PREPARING: '筹备中', CLOSED: '已关闭' }[s] || s)

onMounted(load)
</script>
