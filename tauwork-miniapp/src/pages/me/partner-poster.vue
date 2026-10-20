<template>
  <view class="poster" v-if="loaded && home.partner">
    <view class="top">
      <view class="tag">寻找合伙人</view>
      <view class="logo">
        <image src="/static/logo.png" mode="aspectFit" />
        <text>九千善</text>
        <text class="en">9000 GOODNESS</text>
      </view>
    </view>
    <view class="photo">
      <image src="/static/visual/meeting.jpg" mode="aspectFill" />
      <view class="veil"></view>
    </view>
    <view class="copy">
      <view class="h1">零门槛自由创业</view>
      <view class="h1">分红5分钟到账</view>
      <view class="pair">长期免费静心营</view>
      <view class="pair">共创物心双幸福</view>
    </view>
    <view class="welcome">欢迎CEO、管理、营销、技术、财务、行政、人事等合伙人加盟</view>
    <view class="foot">
      <view class="who">
        <image src="/static/logo.png" mode="aspectFill" />
        <view>
          <view class="nm">{{ home.name }}</view>
          <view class="role">九千善合伙人</view>
        </view>
      </view>
      <view class="scan">
        <view>扫一扫成为合伙人</view>
        <view>发一发开启新创业</view>
      </view>
      <button class="send" open-type="share">发给好友</button>
    </view>
  </view>
  <view v-else-if="loaded" class="empty">当前微信还不是合伙人。绑定已开通的手机号后，重新登录即可生成海报。</view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { loaded: false, home: { partner: false, name: '', code: '' } }
  },
  onShow() { this.load() },
  onShareAppMessage() {
    const code = this.home.code
    return {
      title: `${this.home.name || '九千善'}邀请你成为合伙人`,
      path: code ? `/pages/me/partner-join?partner=${code}` : '/pages/me/partner-join'
    }
  },
  methods: {
    async load() {
      await api.ensureLogin()
      try {
        this.home = await api.get('/api/mini/partner/me', {}, true)
      } catch (e) {
        this.home = { partner: false }
      }
      this.loaded = true
    }
  }
}
</script>

<style scoped>
.poster { min-height: 100vh; background: #C4151C; color: #fff; padding: 28rpx 32rpx 40rpx; }
.top { display: flex; justify-content: space-between; align-items: flex-start; }
.tag { background: rgba(255,255,255,.16); padding: 10rpx 22rpx; border-radius: 8rpx; font-size: 24rpx; letter-spacing: 2rpx; }
.logo { background: #fff; color: #1C4FA0; border-radius: 12rpx; padding: 10rpx 14rpx 8rpx; text-align: center; min-width: 132rpx; }
.logo image { width: 48rpx; height: 48rpx; }
.logo text { display: block; font-size: 22rpx; font-weight: 600; }
.en { font-size: 14rpx !important; letter-spacing: 1rpx; font-weight: 500 !important; }
.photo { margin: 28rpx auto 0; width: 560rpx; height: 420rpx; border-radius: 280rpx 280rpx 0 0; overflow: hidden; position: relative; }
.photo image, .veil { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.veil { background: linear-gradient(180deg, rgba(196,21,28,.15), rgba(196,21,28,.72)); }
.copy { margin-top: -80rpx; position: relative; }
.h1 { font-size: 56rpx; font-weight: 700; letter-spacing: 2rpx; line-height: 1.25; }
.pair { margin-top: 8rpx; font-size: 28rpx; opacity: .92; }
.welcome { margin-top: 36rpx; font-size: 26rpx; line-height: 1.6; opacity: .95; }
.foot { margin-top: 36rpx; background: #fff; color: #1C1915; border-radius: 20rpx; padding: 24rpx; display: flex; align-items: center; gap: 16rpx; }
.who { display: flex; align-items: center; gap: 12rpx; flex: 1; }
.who image { width: 72rpx; height: 72rpx; border-radius: 36rpx; background: #F3EEE6; }
.nm { font-size: 28rpx; font-weight: 600; }
.role { font-size: 22rpx; color: #8A8175; }
.scan { font-size: 22rpx; line-height: 1.5; color: #5C564E; }
.send { margin: 0; padding: 0 18rpx; height: 88rpx; line-height: 88rpx; background: #C4151C; color: #fff; font-size: 24rpx; border-radius: 12rpx; }
.empty { margin: 80rpx 40rpx; color: #8A8175; line-height: 1.6; }
</style>
