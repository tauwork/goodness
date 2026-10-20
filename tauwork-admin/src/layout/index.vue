<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '232px'" class="side">
      <div class="brand">
        <img class="lg" src="../assets/logo.png" alt="logo" />
        <div v-show="!collapsed" class="brand-text">
          <b>九千善Goodness</b>
          <span>智能业务运营系统ABOS</span>
        </div>
      </div>
      <el-menu
        :default-active="route.path"
        :default-openeds="opened"
        :key="collapsed ? 'collapsed' : 'expanded'"
        :collapse="collapsed"
        :collapse-transition="false"
        unique-opened
        router
        class="menu"
        popper-class="side-pop"
      >
        <el-sub-menu v-for="group in menus" :key="group.index" :index="group.index">
          <template #title>
            <el-icon><component :is="group.icon" /></el-icon>
            <span>{{ group.title }}</span>
          </template>
          <el-menu-item v-for="item in group.children" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container class="work">
      <el-header class="top" height="56px">
        <el-tooltip :content="collapsed ? '展开菜单' : '收起菜单'" placement="bottom">
          <button class="fold" type="button" @click="toggle">
            <el-icon :size="18"><Expand v-if="collapsed" /><Fold v-else /></el-icon>
          </button>
        </el-tooltip>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item v-if="current.group">{{ current.group }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ route.meta.title || current.title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <el-dropdown class="user" trigger="click">
          <span class="user-btn">
            <span class="av">{{ avatar }}</span>
            <span class="user-name">{{ user.realName || '管理员' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowDown, Avatar, Bell, Box, Briefcase, Calendar, ChatLineSquare, Coin, DataLine,
  EditPen, Expand, Flag, Fold, Goods, House, Location, Lock, Notebook, OfficeBuilding,
  Odometer, Picture, Setting, Share, ShoppingBag, Tickets, User, UserFilled
} from '@element-plus/icons-vue'
import http from '../utils/http'

const COLLAPSE_KEY = 'abos-menu-collapsed'
const icons = {
  DataLine, Odometer, User, Avatar, Flag, Calendar, EditPen, Location, Tickets, OfficeBuilding,
  Box, Goods, Briefcase, House, ShoppingBag, Notebook, Picture, ChatLineSquare, Bell,
  Setting, UserFilled, Lock, Share, Coin
}

const route = useRoute()
const router = useRouter()
const collapsed = ref(localStorage.getItem(COLLAPSE_KEY) === '1')
const profile = ref((() => {
  try { return JSON.parse(localStorage.getItem('user') || '{}') } catch (e) { return {} }
})())
const user = computed(() => profile.value)
const menus = computed(() => (profile.value.menus || []).map(group => ({
  index: String(group.id),
  title: group.title,
  icon: icons[group.icon] || DataLine,
  children: (group.children || []).filter(item => item.path).map(item => ({
    path: item.path,
    title: item.title,
    icon: icons[item.icon] || DataLine
  }))
})))

onMounted(async () => {
  if (!localStorage.getItem('token')) return
  try {
    const me = await http.get('/auth/me')
    me.token = me.token || localStorage.getItem('token')
    profile.value = me
    localStorage.setItem('user', JSON.stringify(me))
  } catch (e) { /* 登录失效时由接口拦截处理 */ }
})
const avatar = computed(() => (user.value.realName || '运').slice(0, 1))
const opened = computed(() => {
  const hit = menus.value.find(g => g.children.some(c => c.path === route.path))
  return hit ? [hit.index] : []
})
const current = computed(() => {
  for (const group of menus.value) {
    const item = group.children.find(c => c.path === route.path)
    if (item) return { group: group.title, title: item.title }
  }
  return { group: '', title: '' }
})

function toggle(event) {
  collapsed.value = !collapsed.value
  localStorage.setItem(COLLAPSE_KEY, collapsed.value ? '1' : '0')
  event.currentTarget?.blur()
}

function logout() {
  const flag = localStorage.getItem(COLLAPSE_KEY)
  localStorage.clear()
  if (flag) localStorage.setItem(COLLAPSE_KEY, flag)
  router.push('/login')
}
</script>

<style>
html, body, #app { height: 100%; margin: 0; }
body { background: #F4F6FA; font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif; }
.layout { height: 100vh; overflow: hidden; }
.side {
  background: #0E2544;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width .2s ease;
}
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255,255,255,.06);
}
.brand .lg { width: 32px; height: 32px; border-radius: 8px; object-fit: contain; flex-shrink: 0; }
.brand-text { min-width: 0; }
.brand b { color: #fff; font-size: 17px; font-weight: 600; display: block; line-height: 1.2; }
.brand span { color: #C7CFDD; font-size: 12px; font-weight: 400; line-height: 1.3; }
.menu {
  border-right: none;
  flex: 1;
  overflow: auto;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #C7CFDD;
  --el-menu-item-font-size: 13px;
  --el-menu-hover-bg-color: rgba(255,255,255,.08);
  --el-menu-active-color: #fff;
  --el-menu-hover-text-color: #fff;
}
.menu .el-sub-menu__title,
.menu .el-menu-item {
  font-size: 13px;
  font-weight: 400;
}
.menu:not(.el-menu--collapse) .el-sub-menu__title,
.menu:not(.el-menu--collapse) .el-menu-item {
  height: 36px;
  line-height: 36px;
  margin: 2px 10px;
  border-radius: 8px;
  width: auto;
}
.menu .el-menu-item.is-active { background: #C2A05F; font-weight: 600; }
.menu .el-sub-menu.is-active > .el-sub-menu__title { color: #fff; }
.menu .el-sub-menu__icon-arrow { color: #7C93B8; }
.work { min-width: 0; min-height: 0; overflow: hidden; }
.top {
  background: #fff;
  border-bottom: 1px solid #E8ECF3;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
}
.fold {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #22304A;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
}
.fold:hover { background: #F4F6FA; }
.user { margin-left: auto; }
.user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #22304A;
  outline: none;
}
.user-name { font-size: 13px; font-weight: 600; }
.top .el-breadcrumb { font-size: 13px; font-weight: 400; }
.top .el-breadcrumb__item:last-child .el-breadcrumb__inner { font-weight: 600; color: #22304A; }
.av {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #C2A05F;
  color: #0E2544;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
}
.main { padding: 16px; background: #F4F6FA; }
.side-pop.el-menu {
  background: #0E2544;
  border: none;
  --el-menu-bg-color: #0E2544;
  --el-menu-text-color: #C7CFDD;
  --el-menu-item-font-size: 13px;
  --el-menu-hover-bg-color: rgba(255,255,255,.08);
  --el-menu-active-color: #fff;
  --el-menu-hover-text-color: #fff;
}
.side-pop .el-menu-item { font-size: 13px; font-weight: 400; }
.side-pop .el-menu-item.is-active { background: #C2A05F; font-weight: 600; }
</style>
