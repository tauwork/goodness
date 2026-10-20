<template>
  <view class="page" v-if="loaded">
    <view class="hero">
      <view class="kicker">PARTNER</view>
      <view class="h1">成为九千善合伙人</view>
      <view class="sub light">接受邀请后，你带来的客户会计入邀请人的间接客户。</view>
    </view>
    <view class="card" v-if="done">
      <view class="who">你已经是合伙人</view>
      <view class="sub">可以去我的分销查看海报、佣金和客户。</view>
      <button class="btn-gold" @tap="goHub">进入我的分销</button>
    </view>
    <view class="card" v-else>
      <view class="sub">需要已绑定的手机号和姓名。还没填写的话，先去绑定手机号、完善档案。</view>
      <button class="btn-gold" @tap="join">接受邀请</button>
      <button class="plain" @tap="goPhone">去绑定手机号</button>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { loaded: false, code: '', done: false }
  },
  onLoad(options) {
    this.code = (options && options.partner) || ''
  },
  async onShow() {
    await api.ensureLogin()
    const home = await api.get('/api/mini/partner/me', {}, true)
    this.done = !!home.partner
    this.loaded = true
  },
  methods: {
    async join() {
      if (!this.code) return uni.showToast({ title: '缺少邀请', icon: 'none' })
      await api.post('/api/mini/partner/join', { code: this.code })
      this.done = true
      uni.showToast({ title: '已开通', icon: 'success' })
    },
    goHub() { uni.redirectTo({ url: '/pages/me/partner' }) },
    goPhone() { uni.navigateTo({ url: '/pages/me/phone' }) }
  }
}
</script>

<style scoped>
.hero { margin: 24rpx 28rpx; padding: 36rpx 32rpx; border-radius: 28rpx; background: #C4151C; color: #fff; }
.h1 { font-size: 40rpx; margin: 10rpx 0 12rpx; }
.light { color: rgba(255,255,255,.82); }
.who { font-size: 32rpx; margin-bottom: 8rpx; }
.plain { margin-top: 16rpx; background: transparent; color: #8A8175; font-size: 26rpx; }
</style>
