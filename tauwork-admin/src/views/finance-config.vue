<template>
  <el-card shadow="never" style="max-width:640px">
    <template #header><b>收款账户</b></template>
    <p class="hint">保存后，小程序服务下单里的对公转账会展示这些信息。</p>
    <el-form label-width="88px">
      <el-form-item label="户名">
        <el-input v-model="form.accountName" maxlength="64" placeholder="收款户名" />
      </el-form-item>
      <el-form-item label="开户行">
        <el-input v-model="form.bankName" maxlength="128" placeholder="例如招商银行" />
      </el-form-item>
      <el-form-item label="支行">
        <el-input v-model="form.branch" maxlength="128" placeholder="选填" />
      </el-form-item>
      <el-form-item label="账号">
        <el-input v-model="form.accountNo" maxlength="64" placeholder="收款账号" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../utils/http'

const saving = ref(false)
const form = reactive({ accountName: '', bankName: '', accountNo: '', branch: '' })

async function load() {
  const data = await http.get('/admin/finance/account')
  form.accountName = data.accountName || ''
  form.bankName = data.bankName || ''
  form.accountNo = data.accountNo || ''
  form.branch = data.branch || ''
}

async function save() {
  saving.value = true
  try {
    const data = await http.put('/admin/finance/account', { ...form })
    form.accountName = data.accountName || ''
    form.bankName = data.bankName || ''
    form.accountNo = data.accountNo || ''
    form.branch = data.branch || ''
    ElMessage.success('已保存')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.hint { margin: 0 0 18px; color: #8A94A8; font-size: 13px; }
</style>
