<template>
  <view class="page" v-if="p">
    <view class="hero">
      <view class="hname">{{ p.name }}</view>
      <view class="hsub">{{ p.subtitle }}</view>
      <view class="hprice">¥{{ p.priceValue }}<text v-if="p.priceMode==='STAGED'"> 起</text></view>
    </view>

    <view class="card">
      <view class="ft">服务说明</view>
      <view class="body">由九千善专业顾问团队交付，服务边界、交付物清单与执行 SOP 以正式合同附件为准；价格含服务费，政府规费等代收代付项目单列。</view>
      <view class="ft">合规承诺</view>
      <view class="body">不承诺「保录取/保获批/保开户/保下户」；不虚构雇佣、收入、学历或业绩；全部交付物写入合约附件并由客户签收。</view>
    </view>

    <view class="card">
      <view class="ft">预约咨询（首次免费）</view>
      <view class="body sub">顾问一对一沟通家庭情况与目标，出具适配路径建议</view>
      <view class="btn-gold" @tap="book">立即预约咨询</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { p: null }
  },
  onLoad(q) {
    api.get(`/api/mini/svc/product/${q.id}`).then(p => this.p = p)
  },
  methods: {
    book() {
      uni.navigateTo({ url: `/pages/svc/book?id=${this.p.id}` })
    }
  }
}
</script>

<style scoped>
.hero { background: linear-gradient(150deg,#16355E,#2E5C9E); margin: 24rpx; border-radius: 28rpx; padding: 36rpx; color: #fff; }
.hname { font-size: 38rpx; font-weight: 600; }
.hsub { font-size: 24rpx; color: #B9C9E2; margin: 14rpx 0 20rpx; line-height: 1.6; }
.hprice { font-size: 40rpx; font-weight: 700; color: #E3C98A; }
.ft { font-size: 28rpx; font-weight: 600; margin: 24rpx 0 14rpx; }
.body { font-size: 25rpx; line-height: 1.8; color: #3A4658; }
.btn-gold { margin-top: 24rpx; }
</style>
