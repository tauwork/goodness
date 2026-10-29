<template>
  <view class="page">
    <view class="card" v-for="item in list" :key="item.id" @tap="open(item)">
      <view class="head">
        <text class="name">{{ item.companyName }}</text>
        <text class="status">{{ item.stepStatusLabel }}</text>
      </view>
      <view class="sub">{{ item.productName }} · {{ item.orderNo }}</view>
      <view class="foot">
        <text class="step">{{ item.stepLabel }}</text>
        <text class="time">{{ (item.createTime || '').replace('T', ' ').slice(0, 16) }}</text>
      </view>
    </view>
    <view v-if="loaded && !list.length" class="empty">提交公司注册订单后，进度会显示在这里</view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { list: [], loaded: false }
  },
  onShow() {
    api.get('/api/mini/co-reg/my', null, true)
      .then(d => { this.list = d || [] })
      .finally(() => { this.loaded = true })
  },
  methods: {
    open(item) {
      uni.navigateTo({ url: `/pages/me/business-detail?id=${item.id}` })
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
.step { color: #1D1D1F; font-size: 26rpx; font-weight: 500; }
.time { color: #86868B; font-size: 22rpx; }
.empty { text-align: center; color: #86868B; font-size: 26rpx; padding: 120rpx 40rpx; line-height: 1.6; }
</style>
