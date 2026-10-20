<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;gap:10px;align-items:center">
        <b>{{ title }}</b>
        <el-select v-model="categoryId" clearable placeholder="全部分类" style="width:160px" @change="load">
          <el-option v-for="c in cats" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索名称" clearable style="width:180px" @keyup.enter="load" @clear="load" />
        <el-button @click="load">查询</el-button>
        <div style="margin-left:auto;display:flex;gap:8px">
          <el-button @click="catDialog = true">管理分类</el-button>
          <el-button type="primary" @click="openCreate">新建</el-button>
        </div>
      </div>
    </template>

    <el-table :data="list" stripe>
      <el-table-column label="主图" width="72">
        <template #default="{ row }">
          <el-image v-if="row.coverUrl" :src="row.coverUrl" fit="cover" style="width:44px;height:44px;border-radius:6px" />
          <span v-else style="color:#C3CBD8">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column label="分类" width="140">
        <template #default="{ row }">{{ catName(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column label="价格" width="130">
        <template #default="{ row }">
          <span v-if="row.priceValue != null">¥{{ row.priceValue }}</span>
          <el-tag v-else size="small" type="info">{{ priceModeText(row.priceMode) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="prodType === 'GOODS'" prop="stock" label="库存" width="80" />
      <el-table-column v-if="prodType === 'GOODS'" prop="spec" label="规格" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.onSale === 1 ? 'success' : 'info'" size="small">{{ row.onSale === 1 ? '在售' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.onSale === 1" size="small" plain type="warning" @click="toggle(row, false)">下架</el-button>
          <el-button v-else size="small" plain type="success" @click="toggle(row, true)">上架</el-button>
          <el-button size="small" plain type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialog" :title="form.id ? '编辑' + title : '新建' + title" width="640px" destroy-on-close>
    <el-form label-width="88px">
      <el-form-item label="名称" required>
        <el-input v-model="form.name" maxlength="128" />
      </el-form-item>
      <el-form-item label="一句话">
        <el-input v-model="form.subtitle" maxlength="255" />
      </el-form-item>
      <el-form-item label="分类" required>
        <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
          <el-option v-for="c in cats" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item :label="prodType === 'GOODS' ? '主图' : '主图'" :required="prodType === 'GOODS'">
        <div style="display:flex;gap:12px;align-items:center">
          <el-image v-if="form.coverUrl" :src="form.coverUrl" fit="cover" style="width:72px;height:72px;border-radius:8px" />
          <el-upload :action="'/api/admin/file/upload'" :headers="uploadHeaders" :show-file-list="false"
                     accept="image/jpeg,image/png,image/gif,image/webp" :on-success="onUploaded" :on-error="onUploadError">
            <el-button>上传主图</el-button>
          </el-upload>
        </div>
      </el-form-item>
      <el-form-item v-if="prodType === 'SERVICE'" label="计价">
        <el-select v-model="form.priceMode" style="width:180px">
          <el-option label="固定价" value="FIXED" />
          <el-option label="评估后报价" value="QUOTE" />
          <el-option label="按阶段" value="STAGED" />
          <el-option label="会员专享" value="MEMBER" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="prodType !== 'SERVICE' || form.priceMode !== 'QUOTE'" label="价格" :required="prodType === 'GOODS'">
        <el-input-number v-model="form.priceValue" :min="0" :precision="2" :step="1" />
      </el-form-item>
      <template v-if="prodType === 'GOODS'">
        <el-form-item label="库存" required>
          <el-input-number v-model="form.stock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.spec" maxlength="64" placeholder="如 礼盒 / 单件" />
        </el-form-item>
      </template>
      <el-form-item label="详情">
        <el-input v-model="form.detail" type="textarea" :rows="3" maxlength="2000" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sortNo" :min="0" :step="1" />
      </el-form-item>
      <el-form-item label="上架">
        <el-switch v-model="form.onSale" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>

    <template v-if="prodType === 'SPACE' && form.id">
      <div style="font-weight:600;margin:8px 0">上架到社区</div>
      <el-table :data="stores" size="small">
        <el-table-column label="社区" min-width="160">
          <template #default="{ row }">{{ row.community.name }}</template>
        </el-table-column>
        <el-table-column label="店内价格" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.price" :min="0" :precision="2" size="small" :disabled="!!row.bind" />
          </template>
        </el-table-column>
        <el-table-column label="单位" width="110">
          <template #default="{ row }">
            <el-select v-model="row.priceUnit" size="small" :disabled="!!row.bind" style="width:96px">
              <el-option label="小时" value="HOUR" />
              <el-option label="天" value="DAY" />
              <el-option label="月" value="MONTH" />
              <el-option label="次" value="ONCE" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.stock" :min="0" size="small" :disabled="!!row.bind" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button v-if="!row.bind" size="small" type="primary" link @click="bindStore(row)">上架</el-button>
            <el-button v-else size="small" type="danger" link @click="unbindStore(row)">解除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p style="color:#8A94A8;font-size:12px">公司地址、其他空间仍在空间产品里，不并进工位或房间。社区内价格留空则用上面的默认价。</p>
    </template>
    <p v-else-if="prodType === 'SPACE'" style="color:#8A94A8;font-size:12px">先保存商品，再上架到各社区。</p>

    <template #footer>
      <el-button @click="dialog = false">关闭</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="catDialog" title="分类" width="520px">
    <el-table :data="allCats" size="small">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="code" label="编码" width="140" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" link @click="disableCat(row)">停用</el-button>
          <el-button size="small" link type="danger" @click="deleteCat(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="display:flex;gap:8px;margin-top:12px">
      <el-input v-model="catForm.name" placeholder="新分类名称" />
      <el-button type="primary" @click="addCat">添加</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const route = useRoute()
const prodType = computed(() => route.meta.prodType)
const title = computed(() => route.meta.title)
const list = ref([])
const allCats = ref([])
const cats = ref([])
const keyword = ref('')
const categoryId = ref(null)
const dialog = ref(false)
const catDialog = ref(false)
const saving = ref(false)
const stores = ref([])
const catForm = reactive({ name: '' })
const form = reactive(blank())
const uploadHeaders = computed(() => ({ Authorization: localStorage.getItem('token') || '' }))

function blank() {
  const type = prodType.value
  return {
    id: null,
    prodType: type,
    name: '',
    subtitle: '',
    categoryId: null,
    coverUrl: '',
    detail: '',
    priceMode: 'FIXED',
    priceValue: null,
    stock: type === 'GOODS' ? 0 : null,
    spec: '',
    onSale: 1,
    sortNo: 0
  }
}

function resetForm() {
  Object.assign(form, blank())
}

async function load() {
  const type = prodType.value
  const [page, categories] = await Promise.all([
    http.get('/admin/product/page', {
      params: {
        page: 1,
        size: 100,
        prodType: type,
        categoryId: categoryId.value || undefined,
        keyword: keyword.value || undefined
      }
    }),
    http.get('/admin/product-category/list', { params: { prodType: type, onlyEnabled: false } })
  ])
  list.value = page.records
  allCats.value = categories
  cats.value = categories.filter(c => c.status === 1)
}

function catName(id) {
  return allCats.value.find(c => c.id === id)?.name || '-'
}

function openCreate() {
  resetForm()
  stores.value = []
  dialog.value = true
}

async function openEdit(row) {
  resetForm()
  Object.assign(form, {
    id: row.id,
    prodType: row.prodType,
    name: row.name,
    subtitle: row.subtitle,
    categoryId: row.categoryId,
    coverUrl: row.coverUrl || '',
    detail: row.detail || '',
    priceMode: row.priceMode || 'FIXED',
    priceValue: row.priceValue,
    stock: row.stock,
    spec: row.spec || '',
    onSale: row.onSale,
    sortNo: row.sortNo || 0
  })
  dialog.value = true
  if (prodType.value === 'SPACE') await loadStores()
}

async function loadStores() {
  const communities = await http.get('/admin/community/list')
  const rows = []
  for (const community of communities) {
    const binds = await http.get(`/admin/community/${community.id}/product`)
    const bind = binds.find(b => b.productId === form.id) || null
    rows.push({
      community,
      bind,
      price: bind ? bind.price : form.priceValue,
      priceUnit: bind?.priceUnit || 'HOUR',
      stock: bind ? bind.stock : null
    })
  }
  stores.value = rows
}

async function save() {
  saving.value = true
  try {
    const payload = {
      ...form,
      prodType: prodType.value,
      coverUrl: form.coverUrl || null,
      stock: prodType.value === 'GOODS' ? form.stock : null,
      spec: prodType.value === 'GOODS' ? (form.spec || null) : null,
      priceMode: prodType.value === 'GOODS' ? 'FIXED' : form.priceMode
    }
    const saved = form.id ? await http.put('/admin/product', payload) : await http.post('/admin/product', payload)
    form.id = saved.id
    ElMessage.success('已保存')
    await load()
    if (prodType.value === 'SPACE') await loadStores()
    else dialog.value = false
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function toggle(row, on) {
  try {
    await http.put(`/admin/product/${row.id}/${on ? 'on-sale' : 'off-sale'}`)
    ElMessage.success(on ? '已上架' : '已下架')
    load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`删除「${row.name}」？已有订单或店内关联时会拒绝。`, '删除', { type: 'warning' })
  try {
    await http.delete(`/admin/product/${row.id}`)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function onUploaded(res) {
  if (res.code !== 0) {
    ElMessage.error(res.msg || '上传失败')
    return
  }
  form.coverUrl = res.data.url
}

function onUploadError() {
  ElMessage.error('上传失败')
}

async function addCat() {
  if (!catForm.name.trim()) return
  try {
    await http.post('/admin/product-category', { prodType: prodType.value, name: catForm.name.trim() })
    catForm.name = ''
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function disableCat(row) {
  await http.put(`/admin/product-category/${row.id}/disable`)
  load()
}

async function deleteCat(row) {
  try {
    await http.delete(`/admin/product-category/${row.id}`)
    ElMessage.success('已删除分类')
    load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function bindStore(row) {
  try {
    await http.post('/admin/community/bind-product', {
      communityId: row.community.id,
      productId: form.id,
      price: row.price,
      priceUnit: row.priceUnit,
      stock: row.stock
    })
    ElMessage.success('已上架到 ' + row.community.name)
    await loadStores()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function unbindStore(row) {
  try {
    await http.delete(`/admin/community/bind-product/${row.bind.id}`)
    ElMessage.success('已解除')
    await loadStores()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const priceModeText = m => ({ QUOTE: '评估后报价', FIXED: '固定价', STAGED: '按阶段', MEMBER: '会员专享' }[m] || m)

watch(prodType, () => {
  keyword.value = ''
  categoryId.value = null
  dialog.value = false
  load()
}, { immediate: true })
</script>
