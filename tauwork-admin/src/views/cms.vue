<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;align-items:center;gap:10px">
        <b>内容管理</b>
        <el-select v-model="type" style="width:150px" @change="load">
          <el-option label="首页轮播广告" value="BANNER" />
          <el-option label="公告资讯" value="NEWS" />
          <el-option label="客户案例故事" value="CASE_STORY" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索标题" style="width:180px" clearable
                  @keyup.enter="load" @clear="load" />
        <el-button @click="load">搜索</el-button>
        <el-button style="margin-left:auto" type="primary" @click="openCreate">+ 新建内容</el-button>
      </div>
    </template>

    <el-table :data="list" stripe>
      <el-table-column label="" width="60">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" type="warning" effect="dark" size="small">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
      <el-table-column prop="badge" label="角标" width="120" />
      <el-table-column label="跳转" width="110">
        <template #default="{ row }">{{ linkText(row.linkType) }}</template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170">
        <template #default="{ row }">{{ (row.updateTime || '').slice(0, 16) }}</template>
      </el-table-column>
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

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dlg" :title="form.id ? '编辑内容' : '新建内容（草稿）'" width="620px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="60" show-word-limit placeholder="轮播主题 / 资讯标题 / 故事引语" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" maxlength="100" placeholder="一句话说明" />
        </el-form-item>
        <el-form-item label="角标">
          <el-input v-model="form.badge" placeholder="如：3分钟·免费 / 10/17·深圳 / 4周获批" style="width:240px" />
        </el-form-item>
        <el-form-item v-if="type === 'NEWS'" label="正文">
          <el-input v-model="form.body" type="textarea" :rows="6" placeholder="资讯正文" />
        </el-form-item>
        <el-form-item label="样式色">
          <el-select v-model="form.coverStyle" style="width:160px">
            <el-option label="藏青" value="NAVY" />
            <el-option label="金棕" value="GOLD" />
            <el-option label="紫" value="PURPLE" />
            <el-option label="青碧" value="TEAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="跳转">
          <el-select v-model="form.linkType" style="width:160px">
            <el-option label="无跳转" value="NONE" />
            <el-option label="智能评估" value="ASSESS" />
            <el-option label="服务详情" value="SVC" />
            <el-option label="资讯" value="NEWS" />
            <el-option label="活动" value="ACTIVITY" />
          </el-select>
          <el-input v-if="form.linkType === 'SVC'" v-model="form.linkTarget" placeholder="服务产品ID"
                    style="width:140px;margin-left:10px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="info" plain v-if="!form.id" @click="save(true)">存为草稿</el-button>
        <el-button type="primary" :loading="saving" @click="save(false)">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const list = ref([])
const type = ref('BANNER')
const keyword = ref('')
const dlg = ref(false)
const saving = ref(false)

const form = reactive({
  id: null, title: '', subtitle: '', badge: '', body: '',
  coverStyle: 'NAVY', linkType: 'NONE', linkTarget: null
})

const scopeMap = { BANNER: 'HOME_BANNER', NEWS: 'NEWS_LIST', CASE_STORY: 'HOME_STORY' }

async function load() {
  const page = await http.get('/admin/cms/page', {
    params: { page: 1, size: 50, contentType: type.value, keyword: keyword.value || undefined }
  })
  list.value = page.records
}

function openCreate() {
  Object.assign(form, { id: null, title: '', subtitle: '', badge: '', body: '', coverStyle: 'NAVY', linkType: 'NONE', linkTarget: null })
  dlg.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id, title: row.title, subtitle: row.subtitle, badge: row.badge,
    body: row.body || '', coverStyle: row.coverStyle || 'NAVY',
    linkType: row.linkType || 'NONE', linkTarget: row.linkTarget
  })
  dlg.value = true
}

async function save(keepDraft) {
  if (!form.title) return ElMessage.warning('请填写标题')
  saving.value = true
  try {
    const payload = {
      ...form,
      contentType: type.value,
      scope: scopeMap[type.value],
      status: form.id ? undefined : (keepDraft ? 'DRAFT' : 'DRAFT'),
      body: type.value === 'NEWS' ? form.body : null
    }
    if (form.id) await http.put('/admin/cms', payload)
    else await http.post('/admin/cms', payload)
    ElMessage.success(form.id ? '已保存' : '已新建草稿，可随时编辑')
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
const linkText = t => ({ ASSESS: '智能评估', SVC: '服务详情', NEWS: '资讯', ACTIVITY: '活动', NONE: '无跳转' }[t] || t || '无')

onMounted(load)
</script>
