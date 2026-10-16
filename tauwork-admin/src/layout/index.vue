<template>
  <el-container class="layout">
    <el-aside width="216px" class="side">
      <div class="brand">
        <img class="lg" src="../assets/logo.png" alt="logo" />
        <div><b>Tauwork 九千善</b><span>运营管理平台</span></div>
      </div>
      <el-menu :default-active="$route.path" router background-color="transparent" text-color="#B9C9E2"
               active-text-color="#fff" class="menu">
        <div class="grp">经营总览</div>
        <el-menu-item index="/dashboard">经营驾驶舱</el-menu-item>
        <div class="grp">预约与空间</div>
        <el-menu-item index="/svcbookings">服务咨询预约</el-menu-item>
        <el-menu-item index="/bookings">空间预订订单</el-menu-item>
        <el-menu-item index="/communities">社区店管理</el-menu-item>
        <div class="grp">商品与内容</div>
        <el-menu-item index="/products">产品管理</el-menu-item>
        <el-menu-item index="/cms">内容管理</el-menu-item>
      </el-menu>
      <div class="foot">
        <div class="av">运</div>
        <div><b>{{ user.realName }}</b><span>{{ user.roleCode }}</span></div>
        <el-icon style="cursor:pointer;margin-left:auto;color:#7C93B8" @click="logout"><SwitchButton /></el-icon>
      </div>
    </el-aside>
    <el-container>
      <el-header class="top">
        <span class="crumb">{{ $route.meta.title }}</span>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || '{}'))

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<style>
body { margin:0; background:#F4F6FA; font-family:"PingFang SC","Microsoft YaHei",sans-serif; }
.layout { height:100vh; }
.side { background:linear-gradient(180deg,#0E2544,#16355E); display:flex; flex-direction:column; }
.brand { display:flex; align-items:center; gap:10px; padding:18px 16px 10px; }
.brand .lg { width:34px;height:34px;border-radius:10px;object-fit:contain; }
.brand b { color:#fff; font-size:14px; display:block; letter-spacing:1px; }
.brand span { color:#7C93B8; font-size:9.5px; }
.menu { border-right:none; flex:1; }
.grp { color:#5F7699; font-size:10px; letter-spacing:2px; padding:16px 18px 4px; }
.menu .el-menu-item { height:40px; margin:1px 10px; border-radius:9px; }
.menu .el-menu-item.is-active { background:#C2A05F; }
.menu .el-menu-item:hover { background:rgba(255,255,255,.08); }
.foot { padding:14px 16px; border-top:1px solid rgba(255,255,255,.08); display:flex; align-items:center; gap:9px; }
.foot .av { width:32px;height:32px;border-radius:50%;background:#C2A05F;color:#0E2544;
  display:flex;align-items:center;justify-content:center;font-weight:700; }
.foot b { color:#fff; font-size:12px; display:block; }
.foot span { color:#7C93B8; font-size:9.5px; }
.top { background:#fff; border-bottom:1px solid #E8ECF3; display:flex; align-items:center; }
.crumb { font-size:14px; font-weight:600; color:#22304A; }
.main { padding:18px; }
</style>
