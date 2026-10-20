<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;align-items:center;gap:10px">
        <b>{{ kind.name }}</b>
        <el-input v-model="keyword" placeholder="搜索标题" style="width:180px" clearable
                  @keyup.enter="load" @clear="load" />
        <el-button @click="load">搜索</el-button>
        <el-button style="margin-left:auto" type="primary" @click="openCreate">+ {{ kind.create }}</el-button>
      </div>
    </template>

    <el-table :data="list" stripe>
      <el-table-column label="封面" width="88">
        <template #default="{ row }">
          <el-image v-if="row.coverUrl" :src="row.coverUrl" fit="cover" style="width:56px;height:40px;border-radius:6px" />
          <span v-else style="color:#A0A8B8;font-size:12px">默认</span>
        </template>
      </el-table-column>
      <el-table-column label="" width="60">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" type="warning" effect="dark" size="small">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column prop="subtitle" :label="kind.subLabel" min-width="160" show-overflow-tooltip />
      <el-table-column prop="badge" label="角标" width="120" />
      <el-table-column v-if="type === 'BANNER'" label="跳转" width="110">
        <template #default="{ row }">{{ linkText(row.linkType) }}</template>
      </el-table-column>
      <el-table-column v-if="type === 'NEWS'" label="展示日期" width="120">
        <template #default="{ row }">{{ (row.effectiveAt || '').replace('T', ' ').slice(0, 10) || '发布后' }}</template>
      </el-table-column>
      <el-table-column prop="sortNo" label="排序" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'DRAFT' ? 'info' : 'warning'"
                  size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="320" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status !== 'PUBLISHED'" size="small" type="success" plain
                     @click="setStatus(row, 'PUBLISHED')">发布</el-button>
          <el-button v-if="row.status === 'PUBLISHED'" size="small" type="warning" plain
                     @click="setStatus(row, 'OFFLINE')">下架</el-button>
          <el-button size="small" :type="row.isTop === 1 ? 'warning' : 'default'" plain
                     @click="toggleTop(row)">{{ row.isTop === 1 ? '取消置顶' : '置顶' }}</el-button>
          <el-button size="small" type="danger" plain :disabled="row.status === 'PUBLISHED'"
                     @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg" :title="form.id ? ('编辑' + kind.name) : kind.create" width="680px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="120" show-word-limit :placeholder="kind.titlePh" />
        </el-form-item>
        <el-form-item :label="kind.subLabel">
          <el-input v-model="form.subtitle" maxlength="200" :placeholder="kind.subPh" />
        </el-form-item>
        <el-form-item label="角标">
          <el-input v-model="form.badge" maxlength="32" placeholder="如：3分钟 · 免费 / 政策解读 / 4周获批" style="width:320px" />
        </el-form-item>
        <el-form-item label="封面">
          <div style="display:flex;gap:12px;align-items:center">
            <el-image v-if="form.coverUrl" :src="form.coverUrl" fit="cover" style="width:160px;height:96px;border-radius:8px" />
            <el-upload :action="'/api/admin/file/upload'" :headers="uploadHeaders" :show-file-list="false"
                       accept="image/jpeg,image/png,image/gif,image/webp" :on-success="onUploaded" :on-error="() => ElMessage.error('上传失败')">
              <el-button>上传封面</el-button>
            </el-upload>
            <el-button v-if="form.coverUrl" link type="danger" @click="form.coverUrl = ''">去掉封面</el-button>
          </div>
          <div style="color:#8A94A8;font-size:12px;margin-top:4px">{{ kind.coverHint }}</div>
        </el-form-item>
        <el-form-item v-if="type === 'BANNER'" label="无图配色">
          <el-select v-model="form.coverStyle" style="width:160px">
            <el-option label="海港" value="NAVY" />
            <el-option label="礼赠" value="GOLD" />
            <el-option label="身份" value="PURPLE" />
            <el-option label="安居" value="TEAL" />
          </el-select>
          <span style="color:#8A94A8;font-size:12px;margin-left:10px">没上传封面时，小程序用这组默认图</span>
        </el-form-item>
        <el-form-item v-if="type !== 'BANNER'" label="正文">
          <el-input v-model="form.body" type="textarea" :rows="8" :placeholder="kind.bodyPh" />
        </el-form-item>
        <el-form-item v-if="type === 'NEWS'" label="展示日期">
          <el-date-picker v-model="form.effectiveAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss"
                          placeholder="留空则发布后立即展示" />
          <div style="color:#8A94A8;font-size:12px;margin-top:4px">小程序列表和详情显示这一天。填了未来时间，到点才出现。</div>
        </el-form-item>
        <el-form-item v-if="type === 'BANNER'" label="点击跳转">
          <el-select v-model="form.linkType" style="width:160px">
            <el-option label="无跳转" value="NONE" />
            <el-option label="智能评估" value="ASSESS" />
            <el-option label="服务详情" value="SVC" />
            <el-option label="商品详情" value="GOODS" />
            <el-option label="社区" value="SPACE" />
            <el-option label="公告资讯" value="NEWS" />
            <el-option label="客户故事" value="STORY" />
          </el-select>
          <el-input v-if="needsTarget" v-model="form.linkTarget" :placeholder="targetPh" style="width:220px;margin-left:10px" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" :step="1" />
          <span style="color:#8A94A8;font-size:12px;margin-left:10px">数字越大越靠前。置顶仍然排在最前。</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const KINDS = {
  BANNER: {
    name: '广告展示',
    create: '新建广告',
    subLabel: '副标题',
    titlePh: '首页轮播标题',
    subPh: '显示在标题下方的一句话',
    coverHint: '首页「此刻推荐」用这张图。不上传则按配色使用默认图。',
    bodyPh: ''
  },
  CASE_STORY: {
    name: '客户故事',
    create: '新建故事',
    subLabel: '一句话',
    titlePh: '首页卡片上的标题',
    subPh: '卡片底部，如客户身份或服务组合',
    coverHint: '首页卡片和详情页顶部用这张图。不上传则用默认配图。',
    bodyPh: '点进详情后显示的正文。首页卡片只显示标题、角标和一句话。'
  },
  NEWS: {
    name: '公告资讯',
    create: '新建资讯',
    subLabel: '摘要',
    titlePh: '资讯标题',
    subPh: '一句话摘要。正文为空时，详情页用这段',
    coverHint: '列表缩略图和详情页顶部用这张图。不上传则用默认配图。',
    bodyPh: '详情页正文'
  }
}

const route = useRoute()
const type = computed(() => route.meta.contentType || 'BANNER')
const kind = computed(() => KINDS[type.value] || KINDS.BANNER)
const list = ref([])
const keyword = ref('')
const dlg = ref(false)
const saving = ref(false)
const uploadHeaders = computed(() => ({ Authorization: localStorage.getItem('token') || '' }))

const form = reactive(blank())
const needsTarget = computed(() => ['SVC', 'GOODS', 'SPACE', 'NEWS', 'STORY'].includes(form.linkType))
const targetPh = computed(() => ({
  SVC: '服务产品 ID',
  GOODS: '商品 ID',
  SPACE: '社区 ID，留空进社区列表',
  NEWS: '资讯 ID',
  STORY: '故事 ID'
}[form.linkType] || ''))

const scopeMap = { BANNER: 'HOME_BANNER', NEWS: 'NEWS_LIST', CASE_STORY: 'HOME_STORY' }

function blank() {
  return {
    id: null, title: '', subtitle: '', badge: '', body: '',
    coverUrl: '', coverStyle: 'NAVY', linkType: 'NONE', linkTarget: '',
    sortNo: 0, effectiveAt: null
  }
}

async function load() {
  const page = await http.get('/admin/cms/page', {
    params: { page: 1, size: 50, contentType: type.value, keyword: keyword.value || undefined }
  })
  list.value = page.records
}

function openCreate() {
  Object.assign(form, blank())
  dlg.value = true
}

function openEdit(row) {
  Object.assign(form, blank(), {
    id: row.id,
    title: row.title || '',
    subtitle: row.subtitle || '',
    badge: row.badge || '',
    body: row.body || '',
    coverUrl: row.coverUrl || '',
    coverStyle: row.coverStyle || 'NAVY',
    linkType: row.linkType || 'NONE',
    linkTarget: row.linkTarget || '',
    sortNo: row.sortNo || 0,
    effectiveAt: row.effectiveAt || null
  })
  dlg.value = true
}

function onUploaded(res) {
  if (res.code !== 0) {
    ElMessage.error(res.msg || '上传失败')
    return
  }
  form.coverUrl = res.data.url
}

async function save() {
  if (!form.title) return ElMessage.warning('请填写标题')
  saving.value = true
  try {
    const payload = {
      id: form.id,
      title: form.title,
      subtitle: form.subtitle,
      badge: form.badge,
      coverUrl: form.coverUrl || '',
      coverStyle: type.value === 'BANNER' ? form.coverStyle : null,
      sortNo: form.sortNo || 0,
      contentType: type.value,
      scope: scopeMap[type.value],
      body: type.value === 'BANNER' ? null : form.body,
      linkType: type.value === 'BANNER' ? form.linkType : 'NONE',
      linkTarget: type.value === 'BANNER' && needsTarget.value ? (form.linkTarget || '') : '',
      effectiveAt: type.value === 'NEWS' ? form.effectiveAt : null
    }
    if (!form.id) payload.status = 'DRAFT'
    if (form.id) await http.put('/admin/cms', payload)
    else await http.post('/admin/cms', payload)
    ElMessage.success(form.id ? '已保存' : '已存为草稿，发布后才会出现在小程序')
    dlg.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    saving.value = false
  }
}

async function setStatus(row, status) {
  await http.put(`/admin/cms/${row.id}/status/${status}`)
  ElMessage.success(status === 'PUBLISHED' ? '已发布' : '已下架')
  load()
}

async function toggleTop(row) {
  await http.put(`/admin/cms/${row.id}/top`)
  ElMessage.success(row.isTop === 1 ? '已取消置顶' : '已置顶')
  load()
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除「${row.title}」？删除后不可恢复`, '删除确认', { type: 'error' })
  try {
    await http.delete(`/admin/cms/${row.id}`)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const statusText = s => ({ DRAFT: '草稿', PUBLISHED: '已发布', OFFLINE: '已下架' }[s] || s)
const linkText = t => ({
  ASSESS: '智能评估', SVC: '服务详情', GOODS: '商品详情', SPACE: '社区',
  NEWS: '资讯', STORY: '客户故事', ACTIVITY: '活动', NONE: '无跳转'
}[t] || t || '无')

watch(() => route.meta.contentType, () => {
  dlg.value = false
  keyword.value = ''
  load()
})

onMounted(load)
</script>
