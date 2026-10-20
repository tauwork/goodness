<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;align-items:center;gap:12px">
        <b>社区</b>
        <span style="color:#8A94A8;font-size:12px">上架后出现在小程序，按城市、封面、地址和坐标展示</span>
        <el-button style="margin-left:auto" type="primary" @click="openCreate">新建社区</el-button>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column label="封面" width="84">
        <template #default="{ row }">
          <el-image v-if="row.coverUrl" :src="row.coverUrl" fit="cover" style="width:56px;height:56px;border-radius:8px" />
          <span v-else style="color:#C0C4CC">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column label="城市 / 区域" width="140">
        <template #default="{ row }">{{ row.city }}<span v-if="row.district"> · {{ row.district }}</span></template>
      </el-table-column>
      <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
      <el-table-column label="坐标" width="150">
        <template #default="{ row }">
          <span v-if="row.lat != null">{{ row.lat }}, {{ row.lng }}</span>
          <span v-else style="color:#C0C4CC">未填，小程序不显示距离</span>
        </template>
      </el-table-column>
      <el-table-column prop="openHours" label="开放时间" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'OPEN' ? 'success' : 'info'" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status !== 'OPEN'" size="small" link type="success" @click="setSale(row, true)">上架</el-button>
          <el-button v-else size="small" link @click="setSale(row, false)">下架</el-button>
          <el-button size="small" link @click="showProducts(row)">产品</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑社区' : '新建社区'" width="640px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="64" placeholder="如 深圳 · 福田社区" />
        </el-form-item>
        <el-form-item label="城市" required>
          <el-input v-model="form.city" maxlength="32" placeholder="深圳 / 香港" style="width:200px" />
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model="form.district" maxlength="64" placeholder="福田区" style="width:200px" />
        </el-form-item>
        <el-form-item label="地址" required>
          <el-input v-model="form.address" maxlength="255" placeholder="小程序卡片上展示的地址" />
        </el-form-item>
        <el-form-item label="坐标">
          <div style="display:flex;gap:8px;align-items:center">
            <el-input-number v-model="form.lat" :precision="6" :step="0.001" placeholder="纬度" controls-position="right" />
            <el-input-number v-model="form.lng" :precision="6" :step="0.001" placeholder="经度" controls-position="right" />
          </div>
          <div style="color:#8A94A8;font-size:12px;margin-top:4px">纬度在前、经度在后。小程序用它计算距离。</div>
        </el-form-item>
        <el-form-item label="开放时间">
          <el-input v-model="form.openHours" maxlength="64" placeholder="周一至周日 09:00-21:00" />
        </el-form-item>
        <el-form-item label="封面">
          <div style="display:flex;gap:12px;align-items:center">
            <el-image v-if="form.coverUrl" :src="form.coverUrl" fit="cover" style="width:96px;height:72px;border-radius:8px" />
            <el-upload :action="'/api/admin/file/upload'" :headers="uploadHeaders" :show-file-list="false"
                       accept="image/jpeg,image/png,image/gif,image/webp" :on-success="onUploaded" :on-error="() => ElMessage.error('上传失败')">
              <el-button>上传封面</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="设施">
          <el-input v-model="form.facilitiesText" placeholder="投屏，Wi-Fi，茶水。用逗号分开" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="2" maxlength="512" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="咨询到店">
          <el-select v-model="form.bookingMode" clearable placeholder="不承接线下咨询" style="width:220px">
            <el-option label="视频以外 · 香港葵青" value="KWAI_TSING" />
            <el-option label="深圳 · 福田" value="FUTIAN" />
            <el-option label="深圳 · 宝安" value="BAOAN" />
          </el-select>
        </el-form-item>
        <el-form-item label="上架">
          <el-switch v-model="form.status" active-value="OPEN" inactive-value="CLOSED" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawer" :title="(current?.name || '') + ' · 空间产品'" size="640px">
      <el-table :data="bindings" stripe>
        <el-table-column label="产品" min-width="180">
          <template #default="{ row }">{{ productName(row.productId) }}</template>
        </el-table-column>
        <el-table-column label="社区价 / 单位" width="150">
          <template #default="{ row }">¥{{ row.price }} {{ unitText(row.priceUnit) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="数量" width="80" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button size="small" type="danger" plain @click="unbind(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p style="color:#8A94A8;font-size:12px">工位、会议室、房间的数量和起步价，来自这里关联的空间产品。可在「空间产品」里上架到本社区。</p>
    </el-drawer>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const list = ref([])
const dialog = ref(false)
const form = ref(blank())
const drawer = ref(false)
const current = ref(null)
const bindings = ref([])
const products = ref([])
const uploadHeaders = computed(() => ({ Authorization: localStorage.getItem('token') || '' }))

function blank() {
  return {
    id: null, name: '', city: '', district: '', address: '',
    lat: null, lng: null, openHours: '', coverUrl: '',
    facilitiesText: '', intro: '', bookingMode: '', status: 'OPEN', sortNo: 0
  }
}

function statusText(s) {
  return { OPEN: '已上架', CLOSED: '已下架', PREPARING: '筹备中' }[s] || s
}

function unitText(u) {
  return { HOUR: '小时', DAY: '天', MONTH: '月', ONCE: '场' }[u] || u || ''
}

function productName(id) {
  const p = products.value.find(x => String(x.id) === String(id))
  return p ? p.name : id
}

function facilitiesToText(raw) {
  if (!raw) return ''
  try {
    const arr = typeof raw === 'string' ? JSON.parse(raw) : raw
    return Array.isArray(arr) ? arr.join('，') : ''
  } catch (e) {
    return ''
  }
}

function textToFacilities(text) {
  const arr = (text || '').split(/[,，]/).map(s => s.trim()).filter(Boolean)
  return arr.length ? JSON.stringify(arr) : null
}

async function load() {
  list.value = await http.get('/admin/community/list')
}

function openCreate() {
  form.value = blank()
  dialog.value = true
}

function openEdit(row) {
  form.value = {
    ...blank(),
    ...row,
    lat: row.lat == null ? null : Number(row.lat),
    lng: row.lng == null ? null : Number(row.lng),
    facilitiesText: facilitiesToText(row.facilities),
    status: row.status === 'OPEN' ? 'OPEN' : 'CLOSED'
  }
  dialog.value = true
}

function onUploaded(res) {
  if (res.code !== 0) {
    ElMessage.error(res.msg || '上传失败')
    return
  }
  form.value.coverUrl = res.data.url
}

async function save() {
  const payload = {
    ...form.value,
    facilities: textToFacilities(form.value.facilitiesText)
  }
  delete payload.facilitiesText
  if (payload.id) await http.put('/admin/community', payload)
  else {
    delete payload.id
    await http.post('/admin/community', payload)
  }
  ElMessage.success('已保存')
  dialog.value = false
  load()
}

async function setSale(row, on) {
  await http.put(`/admin/community/${row.id}/${on ? 'on' : 'off'}`)
  ElMessage.success(on ? '已上架' : '已下架')
  load()
}

async function showProducts(row) {
  current.value = row
  const [binds, page] = await Promise.all([
    http.get(`/admin/community/${row.id}/product`),
    http.get('/admin/product/page', { params: { prodType: 'SPACE', page: 1, size: 100 } })
  ])
  bindings.value = binds
  products.value = page.records || []
  drawer.value = true
}

async function unbind(row) {
  await ElMessageBox.confirm('确认从该社区移除此产品？', '操作确认', { type: 'warning' })
  await http.delete(`/admin/community/bind-product/${row.id}`)
  ElMessage.success('已移除')
  showProducts(current.value)
}

onMounted(load)
</script>
