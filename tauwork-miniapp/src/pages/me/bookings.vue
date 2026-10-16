<template>
  <view class="page">
    <!-- Tab：空间预订 / 服务预约 -->
    <view class="tabs">
      <view class="tab" :class="{ on: tab === 'space' }" @tap="switchTab('space')">空间预订</view>
      <view class="tab" :class="{ on: tab === 'svc' }" @tap="switchTab('svc')">服务预约</view>
    </view>

    <!-- 空间预订 -->
    <block v-if="tab === 'space'">
      <view class="card bk" v-for="b in list" :key="b.id">
        <view class="bh">
          <text class="bno">{{ b.orderNo }}</text>
          <text class="bstatus" :class="b.status.toLowerCase()">{{ spaceStatus(b.status) }}</text>
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
      <view v-if="loaded && !list.length" class="card empty">暂无空间预订</view>
    </block>

    <!-- 服务预约 -->
    <block v-if="tab === 'svc'">
      <view class="card bk" v-for="b in svcList" :key="b.id">
        <view class="bh">
          <text class="bno">{{ b.topic }}</text>
          <text class="bstatus" :class="b.status.toLowerCase()">{{ svcStatus(b.status) }}</text>
        </view>
        <view class="sub">{{ b.bookDate }} {{ b.timeSlot }} · {{ modeText(b.mode) }}</view>
        <view class="sub" v-if="b.remark">备注：{{ b.remark }}</view>
      </view>
      <view v-if="svcLoaded && !svcList.length" class="card empty">暂无服务预约</view>
    </block>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { tab: 'space', list: [], loaded: false, svcList: [], svcLoaded: false }
  },
  onLoad(q) {
    if (q.tab === 'svc') this.switchTab('svc')
  },
  onShow() {
    this.load()
  },
  methods: {
    switchTab(t) {
      this.tab = t
      if (t === 'svc') this.loadSvc()
      else this.load()
    },
    load() {
      api.get('/api/mini/booking/my', null, true).then(d => this.list = d).finally(() => this.loaded = true)
    },
    loadSvc() {
      api.get('/api/mini/svc-booking/my', null, true).then(d => this.svcList = d).finally(() => this.svcLoaded = true)
    },
    spaceStatus(s) {
      return { PENDING_PAY: '待支付', PAID: '已支付', USED: '已使用', CANCELED: '已退订' }[s] || s
    },
    svcStatus(s) {
      return { PENDING: '待确认', CONFIRMED: '已确认', DONE: '已完成', CANCELED: '已取消', NO_SHOW: '未到访' }[s] || s
    },
    modeText(m) {
      return { VIDEO: '视频咨询', FUTIAN: '深圳·福田', BAOAN: '深圳·宝安', KWAI_TSING: '香港·葵青' }[m] || m
    }
  }
}
</script>

<style scoped>
.tabs { display: flex; gap: 14rpx; padding: 24rpx 24rpx 8rpx; }
.tab { background: #fff; border-radius: 22rpx; padding: 14rpx 30rpx; font-size: 26rpx; color: #8A94A8; }
.tab.on { background: #16355E; color: #fff; font-weight: 600; }
.bh { display: flex; align-items: center; margin-bottom: 8rpx; }
.bno { font-size: 28rpx; font-weight: 600; flex: 1; }
.bstatus { font-size: 22rpx; border-radius: 8rpx; padding: 4rpx 16rpx; }
.bstatus.paid, .bstatus.confirmed { color: #16355E; background: #EDF2F9; }
.bstatus.used, .bstatus.done { color: #2FA36B; background: #E8F5EE; }
.bstatus.canceled, .bstatus.no_show { color: #D9534F; background: #FBEDEC; }
.bstatus.pending_pay, .bstatus.pending { color: #A8853F; background: #F7F1E2; }
.bfoot { display: flex; align-items: center; margin-top: 16rpx; }
.bamt { color: #A8853F; font-size: 32rpx; font-weight: 700; flex: 1; }
.code { display: flex; align-items: center; gap: 12rpx; background: #16355E; border-radius: 14rpx; padding: 12rpx 22rpx; }
.code-label { color: #9DB2D2; font-size: 20rpx; }
.code-num { color: #E3C98A; font-size: 34rpx; font-weight: 700; letter-spacing: 4rpx; }
.empty { text-align: center; color: #8A94A8; }
</style>
