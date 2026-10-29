<template>
  <view class="page">
    <view class="card" v-for="o in list" :key="o.id" @tap="open(o)">
      <view class="head">
        <text class="name">{{ o.productName }}</text>
        <text class="status">{{ statusText(o.status) }}</text>
      </view>
      <view class="sub">{{ o.orderNo }} · {{ channelText(o.payChannel) }}</view>
      <view class="foot">
        <text class="amt">¥{{ money(o.amount) }}</text>
        <text class="time">{{ (o.createTime || '').replace('T', ' ').slice(0, 16) }}</text>
      </view>
    </view>
    <view v-if="loaded && !list.length" class="empty">还没有服务订单</view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { list: [], loaded: false }
  },
  onShow() {
    api.get('/api/mini/svc-order/my', null, true)
      .then(d => { this.list = d || [] })
      .finally(() => { this.loaded = true })
  },
  methods: {
    open(o) { uni.navigateTo({ url: `/pages/me/order?no=${o.orderNo}` }) },
    money(n) {
      const num = Number(n)
      if (Number.isNaN(num)) return n
      return Number.isInteger(num) ? String(num) : num.toFixed(2)
    },
    statusText(s) {
      return { PENDING_PAY: '待支付', CONFIRMING: '待确认到账', PAID: '已支付', CANCELED: '已取消' }[s] || s
    },
    channelText(s) {
      return { WECHAT: '在线支付', TRANSFER: '对公转账' }[s] || ''
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F2F2F7; padding: 16rpx 16rpx 48rpx; }
.card { background: #fff; border-radius: 24rpx; padding: 28rpx; margin-bottom: 16rpx; }
.head { display: flex; justify-content: space-between; gap: 16rpx; align-items: flex-start; }
.name { flex: 1; color: #1D1D1F; font-size: 30rpx; font-weight: 600; line-height: 1.4; }
.status { color: #86868B; font-size: 24rpx; }
.sub { margin-top: 10rpx; color: #86868B; font-size: 24rpx; }
.foot { display: flex; justify-content: space-between; align-items: baseline; margin-top: 18rpx; }
.amt { color: #1D1D1F; font-size: 32rpx; font-weight: 600; }
.time { color: #86868B; font-size: 22rpx; }
.empty { text-align: center; color: #86868B; font-size: 26rpx; padding: 120rpx 0; }
</style>
