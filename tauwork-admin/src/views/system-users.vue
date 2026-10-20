<template>
  <div class="panel">
    <div class="toolbar">
      <b>用户管理</b>
      <span class="grow" />
      <el-button type="primary" @click="open()">新建用户</el-button>
    </div>
    <el-table :data="rows" v-loading="loading">
      <el-table-column prop="username" label="账号" width="140" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机" width="140" />
      <el-table-column prop="roleName" label="角色" width="140" />
      <el-table-column label="归属社区" min-width="200">
        <template #default="{ row }">{{ row.communityNames || '未分配' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{ row }"><el-button link type="primary" @click="open(row)">编辑</el-button></template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑用户' : '新建用户'" width="560px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="账号" required>
          <el-input v-model="form.username" :disabled="!!form.id" maxlength="32" />
        </el-form-item>
        <el-form-item :label="form.id ? '重置密码' : '密码'" :required="!form.id">
          <el-input v-model="form.password" type="password" show-password :placeholder="form.id ? '留空则不修改' : '至少 6 位'" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.realName" maxlength="32" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.roleCode" style="width:100%">
            <el-option v-for="item in roles" :key="item.code" :label="item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属社区">
          <el-select v-model="form.communityIds" multiple collapse-tags style="width:100%" placeholder="系统管理员可见全部，其他角色按这里过滤">
            <el-option v-for="item in communities" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../utils/http'

const rows = ref([])
const roles = ref([])
const communities = ref([])
const loading = ref(false)
const visible = ref(false)
const form = reactive(blank())

function blank() {
  return { id: null, username: '', password: '', realName: '', phone: '', roleCode: '', status: 1, communityIds: [] }
}

async function load() {
  loading.value = true
  try { rows.value = await http.get('/admin/system/user/list') } finally { loading.value = false }
}

async function open(row) {
  if (!roles.value.length) roles.value = await http.get('/admin/system/role/options')
  if (!communities.value.length) communities.value = await http.get('/admin/system/community/options')
  Object.assign(form, row
    ? { ...blank(), ...row, password: '', communityIds: [...(row.communityIds || [])] }
    : blank())
  visible.value = true
}

async function save() {
  if (!form.username.trim() || !form.realName.trim() || !form.roleCode) return ElMessage.warning('请填写账号、姓名和角色')
  if (!form.id && form.password.length < 6) return ElMessage.warning('新建用户密码至少 6 位')
  await http.post('/admin/system/user', { ...form, username: form.username.trim(), realName: form.realName.trim() })
  ElMessage.success('已保存')
  visible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.grow { flex: 1; }
</style>
