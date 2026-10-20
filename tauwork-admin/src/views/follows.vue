<template>
  <el-card shadow="never">
    <template #header>
      <div class="bar">
        <b>跟进日志</b>
        <el-select v-model="followType" placeholder="全部方式" clearable style="width:120px" @change="load">
          <el-option v-for="(label, key) in followMap" :key="key" :label="label" :value="key" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索内容" clearable style="width:200px" @keyup.enter="load" @clear="load" />
        <el-button @click="load">搜索</el-button>
        <el-button type="primary" style="margin-left:auto" @click="openCreate">写日志</el-button>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ time(row.followAt) }}</template>
      </el-table-column>
      <el-table-column prop="customerName" label="客户" width="110" />
      <el-table-column prop="customerPhone" label="手机号" width="130" />
      <el-table-column label="方式" width="80">
        <template #default="{ row }">{{ followMap[row.followType] }}</template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
      <el-table-column label="下次跟进" width="160">
        <template #default="{ row }">{{ time(row.nextFollowAt) }}</template>
      </el-table-column>
      <el-table-column prop="ownerName" label="记录人" width="100" />
    </el-table>
  </el-card>

  <el-dialog v-model="dialog" title="写跟进日志" width="520px">
    <el-form :model="form" label-width="88px">
      <el-form-item label="手机号" required>
        <el-input v-model="form.phone" maxlength="11" @blur="lookup" />
      </el-form-item>
      <el-form-item v-if="matched" label="客户">{{ matched.name }}</el-form-item>
      <el-form-item label="方式">
        <el-select v-model="form.followType" style="width:160px">
          <el-option v-for="(label, key) in followMap" :key="key" :label="label" :value="key" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容" required>
        <el-input v-model="form.content" type="textarea" :rows="5" maxlength="2000" placeholder="客户拜访、会议交流或其他沟通记录" />
      </el-form-item>
      <el-form-item label="下次跟进">
        <el-date-picker v-model="form.nextFollowAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../utils/http'

const followMap = { VISIT: '拜访', MEETING: '会议', CALL: '电话', WECHAT: '微信', OTHER: '其他' }
const list = ref([])
const followType = ref(null)
const keyword = ref('')
const dialog = ref(false)
const matched = ref(null)
const form = reactive({ phone: '', followType: 'VISIT', content: '', nextFollowAt: null })

function time(v) { return v ? String(v).replace('T', ' ').slice(0, 16) : '—' }

async function load() {
  const page = await http.get('/admin/follow/page', {
    params: { page: 1, size: 50, followType: followType.value || undefined, keyword: keyword.value || undefined }
  })
  list.value = page.records
}

function openCreate() {
  Object.assign(form, { phone: '', followType: 'VISIT', content: '', nextFollowAt: null })
  matched.value = null
  dialog.value = true
}

async function lookup() {
  matched.value = null
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return
  matched.value = await http.get('/admin/customer/by-phone', { params: { phone: form.phone } })
}

async function save() {
  if (!matched.value) return ElMessage.warning('请先用手机号找到客户')
  try {
    await http.post('/admin/follow', {
      customerId: matched.value.id,
      followType: form.followType,
      content: form.content,
      nextFollowAt: form.nextFollowAt
    })
    ElMessage.success('日志已保存')
    dialog.value = false
    load()
  } catch (e) { ElMessage.error(e.message) }
}

onMounted(load)
</script>

<style scoped>
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
</style>
