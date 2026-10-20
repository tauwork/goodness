<template>
  <view class="page">
    <view class="intro">
      <text class="kicker">ORDERS</text>
      <view class="intro-title">我的预订</view>
    </view>
    <view class="tabs">
      <view class="tab" :class="{ on: tab === 'space' }" @tap="switchTab('space')">社区</view>
      <view class="tab" :class="{ on: tab === 'svc' }" @tap="switchTab('svc')">咨询</view>
      <view class="tab" :class="{ on: tab === 'goods' }" @tap="switchTab('goods')">商品</view>
    </view>

    <block v-if="tab === 'space'">
      <view class="card bk" v-for="b in list" :key="b.id">
        <image class="bk-img" src="/static/visual/desk.jpg" mode="aspectFill" />
        <view class="bk-body">
          <view class="bh">
            <text class="bno">{{ b.orderNo }}</text>
            <text class="bstatus" :class="b.status.toLowerCase()">{{ spaceStatus(b.status) }}</text>
          </view>
          <view class="sub">{{ b.bookDate }} {{ b.startTime ? b.startTime + ' 起 ' + b.hours + '小时' : '' }}</view>
          <view class="bfoot">
            <text class="bamt">¥{{ b.amount }}</text>
          </view>
          <view v-if="b.entryCode && b.status === 'PAID'" class="code">
            <text class="code-label">入场码</text>
            <text class="code-num">{{ b.entryCode }}</text>
          </view>
        </view>
      </view>
      <view v-if="loaded && !list.length" class="empty-box">
        <image src="/static/visual/desk.jpg" mode="aspectFill" />
        <text>还没有社区预订</text>
      </view>
    </block>

    <block v-if="tab === 'svc'">
      <view class="card bk" v-for="b in svcList" :key="b.id">
        <image class="bk-img" src="/static/visual/journal.jpg" mode="aspectFill" />
        <view class="bk-body">
          <view class="bh">
            <text class="bno">{{ b.topic }}</text>
            <text class="bstatus" :class="b.status.toLowerCase()">{{ svcStatus(b.status) }}</text>
          </view>
          <view class="sub">{{ b.bookDate }} {{ b.timeSlot }} · {{ modeText(b.mode) }}</view>
          <view class="sub" v-if="b.remark">{{ b.remark }}</view>
        </view>
      </view>
      <view v-if="svcLoaded && !svcList.length" class="empty-box">
        <image src="/static/visual/journal.jpg" mode="aspectFill" />
        <text>还没有咨询预约</text>
      </view>
    </block>

    <block v-if="tab === 'goods'">
      <view class="card bk" v-for="b in goodsList" :key="b.id">
        <image class="bk-img" src="/static/visual/gift.jpg" mode="aspectFill" />
        <view class="bk-body">
          <view class="bh">
            <text class="bno">{{ b.productName }}</text>
            <text class="bstatus" :class="b.status.toLowerCase()">{{ goodsStatus(b.status) }}</text>
          </view>
          <view class="sub">{{ b.orderNo }} · ×{{ b.qty }}<text v-if="b.spec"> · {{ b.spec }}</text></view>
          <view class="bfoot">
            <text class="bamt">¥{{ b.amount }}</text>
            <text v-if="b.status === 'PENDING_PAY'" class="act" @tap="pay(b)">支付</text>
            <text v-if="b.status === 'PENDING_PAY'" class="act ghost" @tap="cancel(b)">取消</text>
          </view>
        </view>
      </view>
      <view v-if="goodsLoaded && !goodsList.length" class="empty-box">
        <image src="/static/visual/gift.jpg" mode="aspectFill" />
        <text>还没有商品订单</text>
      </view>
    </block>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { tab: 'space', list: [], loaded: false, svcList: [], svcLoaded: false, goodsList: [], goodsLoaded: false }
  },
  onLoad(q) {
    if (q.tab === 'svc' || q.tab === 'goods') this.switchTab(q.tab)
  },
  onShow() {
    this.load()
  },
  methods: {
    switchTab(t) {
      this.tab = t
      if (t === 'svc') this.loadSvc()
      else if (t === 'goods') this.loadGoods()
      else this.load()
    },
    load() {
      api.get('/api/mini/booking/my', null, true).then(d => this.list = d).finally(() => this.loaded = true)
    },
    loadSvc() {
      api.get('/api/mini/svc-booking/my', null, true).then(d => this.svcList = d).finally(() => this.svcLoaded = true)
    },
    loadGoods() {
      api.get('/api/mini/goods-order/my', null, true).then(d => this.goodsList = d).finally(() => this.goodsLoaded = true)
    },
    pay(b) {
      api.post(`/api/mini/goods-order/mock-pay/${b.orderNo}`, {}, true).then(() => {
        uni.showToast({ title: '支付成功', icon: 'none' })
        this.loadGoods()
      })
    },
    cancel(b) {
      api.put(`/api/mini/goods-order/${b.orderNo}/cancel`, {}, true).then(() => {
        uni.showToast({ title: '已取消', icon: 'none' })
        this.loadGoods()
      })
    },
    goodsStatus(s) {
      return { PENDING_PAY: '待支付', PAID: '已支付', DONE: '已交付', CANCELED: '已取消' }[s] || s
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
.intro { padding: 28rpx 36rpx 8rpx; }
.intro-title { font-size: 44rpx; font-weight: 500; margin-top: 8rpx; }
.tabs { display: flex; margin: 20rpx 28rpx 8rpx; background: #E8E2D8; border-radius: 999rpx; padding: 6rpx; }
.tab { flex: 1; text-align: center; padding: 16rpx 0; border-radius: 999rpx; font-size: 26rpx; color: #6F675E; }
.tab.on { background: #FFFcf8; color: #1C1915; font-weight: 600; }
.bk { display: flex; gap: 0; padding: 0; overflow: hidden; }
.bk-img { width: 168rpx; height: 220rpx; flex-shrink: 0; }
.bk-body { flex: 1; padding: 22rpx 22rpx 18rpx; min-width: 0; }
.bh { display: flex; align-items: flex-start; gap: 12rpx; }
.bno { font-size: 26rpx; font-weight: 500; flex: 1; line-height: 1.4; }
.bstatus { font-size: 20rpx; border-radius: 999rpx; padding: 4rpx 14rpx; flex-shrink: 0; }
.bstatus.paid, .bstatus.confirmed { color: #1C1915; background: #F3EEE6; }
.bstatus.used, .bstatus.done { color: #3E6B52; background: #E7F0EA; }
.bstatus.canceled, .bstatus.no_show { color: #8C4A42; background: #F6E8E4; }
.bstatus.pending_pay, .bstatus.pending { color: #8C6A3D; background: #F6EEDD; }
.bfoot { display: flex; align-items: center; margin-top: 12rpx; }
.bamt { color: #8C6A3D; font-size: 30rpx; font-weight: 600; flex: 1; }
.code { display: flex; align-items: center; gap: 12rpx; margin-top: 14rpx; background: #1C1915; border-radius: 14rpx; padding: 12rpx 18rpx; }
.code-label { color: #C4BBAF; font-size: 20rpx; }
.code-num { color: #F6E7C8; font-size: 32rpx; font-weight: 600; letter-spacing: 4rpx; }
.act { margin-left: 12rpx; background: #1C1915; color: #F6E7C8; border-radius: 999rpx; padding: 8rpx 22rpx; font-size: 22rpx; }
.act.ghost { background: #F3EEE6; color: #6F6252; }
</style>
