<template>
  <view class="page" v-if="loaded">
    <view class="tabs">
      <text :class="{ on: tab === 'ALL' }" @tap="tab = 'ALL'">全部 {{ home.customerCount }}</text>
      <text :class="{ on: tab === 'DIRECT' }" @tap="tab = 'DIRECT'">直接 {{ home.directCount }}</text>
      <text :class="{ on: tab === 'INDIRECT' }" @tap="tab = 'INDIRECT'">间接 {{ home.indirectCount }}</text>
    </view>
    <view v-if="!shown.length" class="empty-box">
      <text>这个范围还没有客户</text>
    </view>
    <view class="card person" v-for="item in shown" :key="item.id">
      <view class="row">
        <text class="who">{{ item.name || '未命名' }}</text>
        <text class="tag">{{ item.relation === 'INDIRECT' ? '间接' : '直接' }}</text>
      </view>
      <view class="sub">{{ item.phone || '未留手机' }} · {{ item.partnerBindSource === 'LINK' ? '来自分享' : '顾问指定' }}</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { loaded: false, tab: 'ALL', home: { customers: [] } }
  },
  computed: {
    shown() {
      const rows = this.home.customers || []
      if (this.tab === 'ALL') return rows
      return rows.filter(item => item.relation === this.tab)
    }
  },
  onShow() { this.load() },
  methods: {
    async load() {
      try {
        await api.ensureLogin()
        this.home = await api.get('/api/mini/partner/me', {}, true)
      } catch (e) {
        this.home = { customers: [] }
      }
      this.loaded = true
    }
  }
}
</script>

<style scoped>
.tabs { display: flex; margin: 24rpx 28rpx; background: #FFFCF8; border-radius: 16rpx; overflow: hidden; }
.tabs text { flex: 1; text-align: center; padding: 22rpx 0; color: #8A8175; font-size: 26rpx; }
.on { color: #1C1915; font-weight: 600; background: #F6E7C8; }
.person + .person { margin-top: 0; border-top: 1rpx solid #F0EBE3; border-radius: 0; }
.row { display: flex; justify-content: space-between; align-items: center; }
.who { font-size: 30rpx; }
.tag { font-size: 22rpx; color: #8C6A3D; background: #F6E7C8; padding: 4rpx 12rpx; border-radius: 999rpx; }
</style>
