<template>
  <el-card shadow="never">
    <template #header>
      <div style="display:flex;align-items:center;gap:12px">
        <b>顾问管理</b>
        <el-select v-model="communityId" clearable placeholder="全部社区" style="width:220px" @change="load">
          <el-option v-for="item in communities" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-button style="margin-left:auto" type="primary" @click="openCreate">新增顾问</el-button>
      </div>
    </template>
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column label="头像" width="76">
        <template #default="{ row }">
          <el-image v-if="row.avatarUrl" :src="row.avatarUrl" fit="cover" style="width:44px;height:44px;border-radius:22px" />
          <span v-else style="color:#C0C4CC">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="title" label="头衔" width="140" show-overflow-tooltip />
      <el-table-column prop="valueService" label="价值服务" min-width="200" show-overflow-tooltip />
      <el-table-column prop="personality" label="性格特点" min-width="160" show-overflow-tooltip />
      <el-table-column prop="wechat" label="微信" width="140" show-overflow-tooltip />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="communityName" label="归属社区" width="160" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑顾问' : '新增顾问'" width="560px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" maxlength="32" />
        </el-form-item>
        <el-form-item label="头像">
          <div style="display:flex;gap:12px;align-items:center">
            <el-image v-if="form.avatarUrl" :src="form.avatarUrl" fit="cover" style="width:64px;height:64px;border-radius:32px" />
            <el-upload :action="'/api/admin/file/upload'" :headers="uploadHeaders" :show-file-list="false"
                       accept="image/jpeg,image/png,image/gif,image/webp" :on-success="onUploaded" :on-error="() => ElMessage.error('上传失败')">
              <el-button>上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="头衔">
          <el-input v-model="form.title" maxlength="64" placeholder="如 首席身份顾问" />
        </el-form-item>
        <el-form-item label="价值服务">
          <el-input v-model="form.valueService" type="textarea" :rows="3" maxlength="500" placeholder="能为客户解决什么" />
        </el-form-item>
        <el-form-item label="性格特点">
          <el-input v-model="form.personality" type="textarea" :rows="2" maxlength="500" placeholder="沟通风格、性格特点" />
        </el-form-item>
        <el-form-item label="微信号">
          <el-input v-model="form.wechat" maxlength="64" placeholder="小程序 1v1 咨询展示" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" maxlength="20" placeholder="小程序 1v1 咨询展示" />
        </el-form-item>
        <el-form-item label="归属社区" required>
          <el-select v-model="form.communityId" filterable style="width:100%">
            <el-option v-for="item in communities" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const rows = ref([])
const communities = ref([])
const loading = ref(false)
const communityId = ref(null)
const dialog = ref(false)
const form = ref({})
const uploadHeaders = computed(() => ({ Authorization: localStorage.getItem('token') || '' }))

function blank() {
  return { id: null, name: '', avatarUrl: '', title: '', valueService: '', personality: '', wechat: '', phone: '', communityId: communityId.value }
}

async function load() {
  loading.value = true
  try {
    rows.value = await http.get('/admin/consultant/list', { params: { communityId: communityId.value || undefined } })
  } finally { loading.value = false }
}

function openCreate() {
  form.value = blank()
  dialog.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialog.value = true
}

function onUploaded(res) {
  if (res.code !== 0) return ElMessage.error(res.msg || '上传失败')
  form.value.avatarUrl = res.data.url
}

async function save() {
  if (!form.value.name || !form.value.name.trim()) return ElMessage.warning('请填写姓名')
  if (!form.value.communityId) return ElMessage.warning('请选择归属社区')
  const payload = { ...form.value, name: form.value.name.trim() }
  if (payload.id) await http.put('/admin/consultant', payload)
  else await http.post('/admin/consultant', payload)
  ElMessage.success('已保存')
  dialog.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`删除顾问「${row.name}」？`, '删除顾问')
  await http.delete(`/admin/consultant/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  communities.value = await http.get('/admin/community/list')
  load()
})
</script>
