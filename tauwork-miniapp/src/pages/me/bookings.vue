<template>
  <view class="page">
    <view class="card bk" v-for="b in list" :key="b.id">
      <view class="bh">
        <text class="bno">{{ b.orderNo }}</text>
        <text class="bstatus" :class="b.status.toLowerCase()">{{ statusText(b.status) }}</text>
      </view>
      <view class="sub">{{ b.bookDate }} {{ b.startTime ? b.startTime + ' 起 ' + b.hours + '小时' : '' }}</view>
      <view class="bfoot">
        <text class="bamt">¥{{ b.amount }}</text>
        <view v-if="b.entryCode && b.status === 'PAID'" class="code">
          <text class="code-label">入场码</text>
          <text class="code-num">{{ b.entryCode }}</text>
        </view>
      </view>
    </view>
    <view v-if="loaded && !list.length" class="card" style="text-align:center;color:#8A94A8">
      暂无预订，去空间页预订一个场地吧
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { list: [], loaded: false }
  },
  onShow() {
    api.get('/api/mini/booking/my', null, true).then(d => this.list = d).finally(() => this.loaded = true)
  },
  methods: {
    statusText(s) {
      return { PENDING_PAY: '待支付', PAID: '已支付', USED: '已使用', CANCELED: '已退订' }[s] || s
    }
  }
}
</script>

<style scoped>
.bh { display: flex; align-items: center; margin-bottom: 8rpx; }
.bno { font-size: 28rpx; font-weight: 600; flex: 1; }
.bstatus { font-size: 22rpx; border-radius: 8rpx; padding: 4rpx 16rpx; }
.bstatus.paid { color: #16355E; background: #EDF2F9; }
.bstatus.used { color: #2FA36B; background: #E8F5EE; }
.bstatus.canceled { color: #D9534F; background: #FBEDEC; }
.bstatus.pending_pay { color: #A8853F; background: #F7F1E2; }
.bfoot { display: flex; align-items: center; margin-top: 16rpx; }
.bamt { color: #A8853F; font-size: 32rpx; font-weight: 700; flex: 1; }
.code { display: flex; align-items: center; gap: 12rpx; background: #16355E; border-radius: 14rpx; padding: 12rpx 22rpx; }
.code-label { color: #9DB2D2; font-size: 20rpx; }
.code-num { color: #E3C98A; font-size: 34rpx; font-weight: 700; letter-spacing: 4rpx; }
</style>
