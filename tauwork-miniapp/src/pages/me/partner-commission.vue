<template>
  <view class="page" v-if="loaded">
    <view class="block">
      <view class="title">客户统计</view>
      <view class="grid">
        <view><text class="n">{{ home.customerCount }}</text><text>全部客户</text></view>
        <view><text class="n">{{ home.directCount }}</text><text>直接客户</text></view>
        <view><text class="n">{{ home.indirectCount }}</text><text>间接客户</text></view>
      </view>
    </view>
    <view class="block">
      <view class="title">佣金统计</view>
      <view class="banner">
        <view><text>总佣金</text><text class="n">¥{{ money(home.commissionAmount) }}</text></view>
        <view><text>已提佣金</text><text class="n">¥{{ money(home.withdrawnAmount) }}</text></view>
        <view><text>佣金余额</text><text class="n">¥{{ money(home.balanceAmount) }}</text></view>
      </view>
      <view class="line">
        <text class="dot d">直</text><text class="lab">直接客户佣金</text><text class="amt">¥{{ money(home.directAmount) }}</text>
      </view>
      <view class="line">
        <text class="dot i">间</text><text class="lab">间接客户佣金</text><text class="amt">¥{{ money(home.indirectAmount) }}</text>
      </view>
      <view class="line">
        <text class="dot o">其</text><text class="lab">其它佣金</text><text class="amt">¥{{ money(home.otherAmount) }}</text>
      </view>
    </view>
    <view class="block" v-if="Number(home.pendingAmount) > 0">
      <view class="sub">提现审核中 ¥{{ money(home.pendingAmount) }}</view>
    </view>
    <view class="block">
      <view class="title">申请提现</view>
      <input class="field" type="digit" v-model="amount" placeholder="不超过佣金余额" />
      <button class="btn-gold" @tap="withdraw">提交申请</button>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { loaded: false, amount: '', home: {} }
  },
  onShow() { this.load() },
  methods: {
    money(v) { return Number(v || 0).toFixed(2) },
    async load() {
      try {
        await api.ensureLogin()
        this.home = await api.get('/api/mini/partner/me', {}, true)
      } catch (e) {
        this.home = {}
      }
      this.loaded = true
    },
    async withdraw() {
      const amount = Number(this.amount)
      if (!(amount > 0)) return uni.showToast({ title: '请填写金额', icon: 'none' })
      await api.post('/api/mini/partner/withdraw', { amount })
      uni.showToast({ title: '已提交', icon: 'success' })
      this.amount = ''
      this.load()
    }
  }
}
</script>

<style scoped>
.block { margin: 24rpx 28rpx; padding: 28rpx; background: #FFFCF8; border-radius: 24rpx; border: 1rpx solid rgba(28,25,21,.06); }
.title { font-size: 28rpx; margin-bottom: 20rpx; }
.grid, .banner { display: flex; }
.grid view, .banner view { flex: 1; text-align: center; }
.grid text, .banner text { display: block; color: #8A8175; font-size: 22rpx; }
.n { color: #1C1915 !important; font-size: 40rpx; margin-bottom: 8rpx; }
.banner { background: linear-gradient(90deg, #3D4C9A, #7A4E8C); color: #fff; border-radius: 16rpx; padding: 28rpx 8rpx; }
.banner text { color: rgba(255,255,255,.8); }
.banner .n { color: #fff !important; font-size: 34rpx; }
.line { display: flex; align-items: center; gap: 16rpx; padding: 22rpx 0; border-bottom: 1rpx solid #F0EBE3; }
.lab { flex: 1; }
.amt { font-size: 30rpx; }
.dot { width: 44rpx; height: 44rpx; border-radius: 22rpx; color: #fff; font-size: 22rpx; text-align: center; line-height: 44rpx; }
.d { background: #5B6BB5; }
.i { background: #C46A8A; }
.o { background: #3AA0C8; }
.field { background: #F6F1EA; border-radius: 12rpx; padding: 18rpx 20rpx; margin-bottom: 20rpx; }
</style>
