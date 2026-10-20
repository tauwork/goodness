<template>
  <div class="panel">
    <div class="toolbar">
      <b>角色权限</b>
      <span class="grow" />
      <el-button type="primary" @click="open()">新建角色</el-button>
    </div>
    <el-table :data="rows" v-loading="loading">
      <el-table-column prop="name" label="角色" width="160" />
      <el-table-column prop="code" label="编码" width="150" />
      <el-table-column prop="remark" label="说明" min-width="220" show-overflow-tooltip />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">{{ row.builtin === 1 ? '内置' : '自定义' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="open(row)">权限</el-button>
          <el-button v-if="row.builtin !== 1" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="visible" :title="form.id ? '配置角色' : '新建角色'" size="460px">
      <el-form :model="form" label-width="72px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="32" />
        </el-form-item>
        <el-form-item label="编码" required>
          <el-input v-model="form.code" :disabled="form.builtin === 1" maxlength="32" placeholder="大写字母，如 ADVISOR" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="权限">
          <el-tree
            ref="treeRef"
            :data="tree"
            node-key="id"
            show-checkbox
            default-expand-all
            :props="{ label: 'title', children: 'children' }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const rows = ref([])
const tree = ref([])
const loading = ref(false)
const visible = ref(false)
const treeRef = ref()
const form = reactive({ id: null, code: '', name: '', remark: '', status: 1, builtin: 0, menuIds: [] })

async function load() {
  loading.value = true
  try { rows.value = await http.get('/admin/system/role/list') } finally { loading.value = false }
}

function leaves(nodes, assigned) {
  const keys = []
  const walk = list => (list || []).forEach(node => {
    const children = node.children || []
    if (!children.length) {
      if (assigned.has(node.id)) keys.push(node.id)
    } else {
      walk(children)
    }
  })
  walk(nodes)
  return keys
}

async function open(row) {
  if (!tree.value.length) tree.value = await http.get('/admin/system/menu/tree')
  Object.assign(form, row
    ? { ...row, menuIds: [...(row.menuIds || [])] }
    : { id: null, code: '', name: '', remark: '', status: 1, builtin: 0, menuIds: [] })
  visible.value = true
  await nextTick()
  const assigned = new Set((form.menuIds || []).map(id => String(id)))
  treeRef.value?.setCheckedKeys(leaves(tree.value, assigned))
}

async function save() {
  if (!form.name.trim() || !form.code.trim()) return ElMessage.warning('请填写名称和编码')
  const checked = treeRef.value?.getCheckedKeys() || []
  const half = treeRef.value?.getHalfCheckedKeys() || []
  await http.post('/admin/system/role', {
    ...form,
    name: form.name.trim(),
    code: form.code.trim().toUpperCase(),
    menuIds: [...checked, ...half]
  })
  ElMessage.success('已保存')
  visible.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`删除角色「${row.name}」？`, '确认')
  await http.delete(`/admin/system/role/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.grow { flex: 1; }
</style>
