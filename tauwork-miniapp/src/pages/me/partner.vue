<template>
  <view class="page sink" v-if="loaded">
    <view v-if="!home.partner" class="empty-box">
      <image src="/static/visual/identity.jpg" mode="aspectFill" />
      <text>当前微信还不是合伙人。后台开通，或绑定已开通的手机号后，重新登录即可看到佣金</text>
    </view>
    <template v-else>
      <view class="hero">
        <view class="kicker">PARTNER</view>
        <view class="name">{{ home.name }}</view>
        <view class="sub light">客户 {{ home.customerCount }} · 可提现 ¥{{ money(home.balanceAmount) }}</view>
      </view>
    </template>
    <view class="menu">
        <view class="mitem" v-for="m in menus" :key="m.url" @tap="go(m.url)">
          <image class="mico" :src="m.icon" mode="aspectFit" />
          <view class="mbody">
            <view class="mtitle">{{ m.title }}</view>
            <view class="msub">{{ m.sub }}</view>
          </view>
          <text class="arrow">›</text>
        </view>
      </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return {
      loaded: false,
      home: { partner: false },
      menus: [
        { icon: '/static/icons/share.png', title: '合伙人海报', sub: '邀请好友成为合伙人', url: '/pages/me/partner-poster' },
        { icon: '/static/icons/coin.png', title: '我的佣金', sub: '直接、间接和可提现余额', url: '/pages/me/partner-commission' },
        { icon: '/static/icons/family.png', title: '我的客户', sub: '直接客户和间接客户', url: '/pages/me/partner-customers' }
      ]
    }
  },
  onShow() { this.load() },
  methods: {
    money(v) { return Number(v || 0).toFixed(2) },
    go(url) { uni.navigateTo({ url }) },
    async load() {
      try {
        await api.ensureLogin()
        this.home = await api.get('/api/mini/partner/me', {}, true)
        if (this.home && this.home.code) uni.setStorageSync('myPartnerCode', this.home.code)
      } catch (e) {
        this.home = { partner: false }
      }
      this.loaded = true
    }
  }
}
</script>

<style scoped>
.hero { margin: 24rpx 28rpx; padding: 36rpx 32rpx; border-radius: 28rpx; background: #1C1915; color: #F6E7C8; }
.name { font-size: 40rpx; margin: 8rpx 0 12rpx; }
.light { color: rgba(246,231,200,.72); }
.menu { margin: 8rpx 28rpx; background: #FFFCF8; border-radius: 24rpx; border: 1rpx solid rgba(28,25,21,.06); }
.mitem { display: flex; align-items: center; gap: 20rpx; padding: 28rpx 24rpx; }
.mitem + .mitem { border-top: 1rpx solid #F0EBE3; }
.mico { width: 40rpx; height: 40rpx; }
.mbody { flex: 1; }
.mtitle { font-size: 30rpx; }
.msub { margin-top: 4rpx; color: #8A8175; font-size: 22rpx; }
.arrow { color: #C4B8A8; font-size: 36rpx; }
</style>
