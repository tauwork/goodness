<template>
  <view class="page sink" v-if="loaded">
    <view v-if="!home.partner" class="empty-box">
      <image src="/static/visual/identity.jpg" mode="aspectFill" />
      <text>开通合伙人后，把小程序分享给客户，成交会计入你的提成</text>
    </view>
    <template v-else>
      <view class="hero">
        <view class="kicker">PARTNER</view>
        <view class="name">{{ home.name }}</view>
        <view class="stats">
          <view><text class="num">¥{{ money(home.commissionAmount) }}</text><text class="lab">提成</text></view>
          <view><text class="num">{{ home.dealCount }}</text><text class="lab">成交</text></view>
          <view><text class="num">{{ home.customerCount }}</text><text class="lab">客户</text></view>
        </view>
        <button class="btn-gold share" open-type="share">分享给客户</button>
      </view>

      <view class="card" v-if="!(home.deals || []).length">
        <view class="sub">还没有成交。客户从你的链接进入后，购买或由顾问登记成交，会出现在这里。</view>
      </view>
      <view class="card deal" v-for="item in home.deals" :key="item.id">
        <view class="row">
          <text class="who">{{ item.customerName || '客户' }}</text>
          <text class="fee">¥{{ money(item.commissionAmount) }}</text>
        </view>
        <view class="sub">{{ item.productName }} · {{ item.categoryName }} · 成交 ¥{{ money(item.orderAmount) }} · {{ item.rate }}%</view>
      </view>

      <view class="section">绑定客户</view>
      <view class="card person" v-for="item in home.customers" :key="item.id">
        <view class="who">{{ item.name || '未命名' }}</view>
        <view class="sub">{{ item.phone || '未留手机' }} · {{ item.partnerBindSource === 'LINK' ? '来自分享' : '顾问指定' }}</view>
      </view>
    </template>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { loaded: false, home: { partner: false, deals: [], customers: [] } }
  },
  onShow() { this.load() },
  onShareAppMessage() {
    const code = this.home.code
    return {
      title: '九千善',
      path: code ? `/pages/index/index?partner=${code}` : '/pages/index/index'
    }
  },
  methods: {
    money(v) { return Number(v || 0).toFixed(2) },
    async load() {
      await api.ensureLogin()
      this.home = await api.get('/api/mini/partner/me', {}, true)
      if (this.home.code) uni.setStorageSync('myPartnerCode', this.home.code)
      this.loaded = true
    }
  }
}
</script>

<style scoped>
.hero { margin: 24rpx 28rpx; padding: 36rpx 32rpx; border-radius: 28rpx; background: #1C1915; color: #F6E7C8; }
.name { font-size: 40rpx; margin: 8rpx 0 24rpx; }
.stats { display: flex; }
.stats view { flex: 1; }
.num { display: block; font-size: 36rpx; }
.lab { display: block; margin-top: 6rpx; font-size: 22rpx; color: rgba(246,231,200,.7); }
.share { margin-top: 28rpx; }
.deal + .deal, .person + .person { margin-top: 0; border-top: 1rpx solid #F0EBE3; border-radius: 0; }
.row { display: flex; justify-content: space-between; align-items: center; }
.who { font-size: 30rpx; }
.fee { color: #8C6A3D; font-size: 32rpx; }
.section { margin: 8rpx 36rpx 12rpx; color: #8A8175; font-size: 24rpx; letter-spacing: 2rpx; }
</style>
