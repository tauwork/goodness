<template>
  <view class="page">
    <view class="intro">
      <image class="intro-img" src="/static/visual/journal.jpg" mode="aspectFill" />
      <view class="intro-copy">
        <text class="kicker">CONTACT</text>
        <view class="intro-title">手机号</view>
        <view class="intro-desc">顾问回访和进度通知，都走这个号码。</view>
      </view>
    </view>

    <view class="card" v-if="bound">
      <view class="kicker">BOUND</view>
      <view class="ft">已绑定</view>
      <view class="phone">{{ masked }}</view>
      <view class="sub">预约与案件进度会通过短信通知您</view>
    </view>

    <view class="card" v-else>
      <view class="kicker">BIND</view>
      <view class="ft">绑定手机号</view>
      <input class="inp" v-model="phone" type="number" maxlength="11" placeholder="请输入手机号" />
      <view class="coderow">
        <input class="inp codeinp" v-model="code" type="number" maxlength="6" placeholder="验证码" />
        <view class="codebtn" :class="{ dis: counting }" @tap="sendCode">{{ counting ? countdown + 's' : '获取验证码' }}</view>
      </view>
      <view class="btn-main" @tap="bind">绑定</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { bound: false, masked: '', phone: '', code: '', counting: false, countdown: 60 }
  },
  onShow() { this.load() },
  methods: {
    async load() {
      const s = await api.get('/api/mini/phone/status', null, true)
      this.bound = s.bound
      this.masked = s.masked
    },
    async sendCode() {
      if (this.counting) return
      if (!/^1[3-9]\d{9}$/.test(this.phone)) return uni.showToast({ title: '手机号格式不正确', icon: 'none' })
      await api.post('/api/mini/phone/send-code', { phone: this.phone })
      uni.showToast({ title: '验证码已发送（开发环境见后端日志）', icon: 'none' })
      this.counting = true
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) { clearInterval(timer); this.counting = false; this.countdown = 60 }
      }, 1000)
    },
    async bind() {
      if (!this.code) return uni.showToast({ title: '请输入验证码', icon: 'none' })
      const bound = await api.post('/api/mini/phone/bind', { phone: this.phone, code: this.code })
      if (bound && bound.token) uni.setStorageSync('token', bound.token)
      if (bound && bound.customerId) uni.setStorageSync('customerId', bound.customerId)
      uni.showToast({ title: '绑定成功', icon: 'success' })
      this.load()
    }
  }
}
</script>

<style scoped>
.intro { position: relative; height: 280rpx; margin: 20rpx 28rpx 8rpx; border-radius: 28rpx; overflow: hidden; }
.intro-img { width: 100%; height: 100%; }
.intro-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 24rpx 28rpx; color: #fff; background: linear-gradient(transparent, rgba(16,14,12,.7)); }
.intro-title { font-size: 40rpx; font-weight: 500; margin-top: 6rpx; }
.intro-desc { font-size: 22rpx; color: rgba(255,255,255,.82); margin-top: 6rpx; }
.kicker { color: #E7D3AE; }
.card .kicker { color: #9A7B4F; }
.ft { font-size: 32rpx; margin: 8rpx 0 12rpx; }
.phone { font-size: 48rpx; font-weight: 500; letter-spacing: 2rpx; margin: 8rpx 0 12rpx; }
.inp { background: #F3EEE6; border-radius: 16rpx; padding: 22rpx 24rpx; font-size: 26rpx; margin-top: 16rpx; }
.coderow { display: flex; gap: 16rpx; align-items: center; }
.codeinp { flex: 1; }
.codebtn { margin-top: 16rpx; background: #1C1915; color: #F6E7C8; font-size: 24rpx; border-radius: 16rpx; padding: 22rpx 24rpx; white-space: nowrap; }
.codebtn.dis { opacity: .45; }
.btn-main { margin-top: 28rpx; }
</style>
