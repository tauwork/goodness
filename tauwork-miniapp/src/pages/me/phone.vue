<template>
  <view class="page">
    <view class="card" v-if="bound">
      <view class="ft">已绑定手机号</view>
      <view class="phone">{{ masked }}</view>
      <view class="sub">预约与案件进度将通过短信通知您</view>
    </view>

    <view class="card" v-else>
      <view class="ft">绑定手机号</view>
      <view class="sub">绑定后顾问可与您电话联系，进度短信通知</view>
      <input class="inp" v-model="phone" type="number" maxlength="11" placeholder="请输入手机号" style="margin-top:20rpx" />
      <view class="coderow">
        <input class="inp" v-model="code" type="number" maxlength="6" placeholder="验证码" />
        <view class="codebtn" :class="{ dis: counting }" @tap="sendCode">{{ counting ? `${countdown}s` : '获取验证码' }}</view>
      </view>
      <view class="btn-main" style="margin-top:24rpx" @tap="bind">绑定手机号</view>
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
      await api.post('/api/mini/phone/bind', { phone: this.phone, code: this.code })
      uni.showToast({ title: '绑定成功', icon: 'success' })
      this.load()
    }
  }
}
</script>

<style scoped>
.ft { font-size: 28rpx; font-weight: 600; margin-bottom: 8rpx; }
.phone { font-size: 40rpx; font-weight: 700; color: #16355E; margin: 12rpx 0; }
.inp { background: #F4F6FA; border-radius: 14rpx; padding: 20rpx 24rpx; font-size: 26rpx; }
.coderow { display: flex; gap: 16rpx; margin-top: 16rpx; align-items: center; }
.coderow .inp { flex: 1; }
.codebtn { background: #16355E; color: #fff; font-size: 24rpx; border-radius: 14rpx; padding: 22rpx 24rpx; white-space: nowrap; }
.codebtn.dis { opacity: .5; }
</style>
