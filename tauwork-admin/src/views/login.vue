<template>
  <div class="login-page">
    <div class="login-card">
      <img class="logo" src="../assets/logo.png" alt="logo" />
      <h2>九千善Goodness</h2>
      <p class="sub">智能业务运营系统ABOS</p>
      <el-form :model="form" @keyup.enter="doLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="账号" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="doLogin">登 录</el-button>
      </el-form>
      <p class="tip">默认账号 admin / admin123</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../utils/http'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

async function doLogin() {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号密码')
  loading.value = true
  try {
    const data = await http.post('/auth/login', form)
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data))
    ElMessage.success(`欢迎，${data.realName}`)
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { height:100vh; display:flex; align-items:center; justify-content:center;
  background:linear-gradient(160deg,#0E2544 0%,#16355E 100%); }
.login-card { width:360px; background:#fff; border-radius:18px; padding:38px 34px 26px; text-align:center; }
.logo { width:56px;height:56px;border-radius:16px;margin:0 auto 14px;object-fit:contain; }
h2 { font-size:17px;font-weight:600;color:#22304A;margin-bottom:2px; }
.sub { color:#6B7C93;font-size:13px;font-weight:400;margin:4px 0 24px; }
.tip { color:#B7BFCF;font-size:11px;margin-top:16px; }
</style>
