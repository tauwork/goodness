<template>
  <view class="page sink" v-if="p">
    <view class="hero">
      <image class="hero-img" :src="image" mode="aspectFill" />
      <view class="hero-shade"></view>
      <view class="hero-copy">
        <text class="hero-en">{{ en }}</text>
        <view class="hname">{{ p.name }}</view>
        <view class="hsub">{{ p.subtitle }}</view>
        <view class="hprice">¥{{ p.priceValue }}<text v-if="p.priceMode==='STAGED'"> 起</text></view>
      </view>
    </view>

    <view class="card sheet">
      <view class="kicker">ABOUT</view>
      <view class="ft">服务说明</view>
      <view class="body">由九千善顾问团队交付。服务边界、交付物与执行方式写入正式合同附件；价格含服务费，政府规费等代收代付另行单列。</view>
      <view class="ft">我们如何承诺</view>
      <view class="body">不承诺保录取、保获批、保开户或保下户。不虚构雇佣、收入、学历或业绩。交付物写入合约，由客户签收。</view>
    </view>

    <view class="card consult">
      <image class="consult-img" src="/static/visual/journal.jpg" mode="aspectFill" />
      <view class="consult-body">
        <view class="kicker">FIRST SESSION</view>
        <view class="ft">预约咨询，首次免费</view>
        <view class="body">顾问一对一了解家庭情况与目标，再给出适配路径。</view>
      </view>
    </view>

    <view class="paybar">
      <view>
        <view class="plabel">首次咨询</view>
        <view class="pamount">免费</view>
      </view>
      <view class="btn-gold paybtn" @tap="book">预约顾问</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'
import { serviceImage, lineEn } from '../../utils/visual'

export default {
  data() {
    return { p: null }
  },
  computed: {
    image() { return this.p ? serviceImage(this.p) : '' },
    en() { return this.p ? lineEn(this.p.bizLine, this.p.name) : '' }
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
.hero { position: relative; height: 560rpx; }
.hero-img, .hero-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.hero-shade { background: linear-gradient(180deg, rgba(16,14,12,.05) 20%, rgba(16,14,12,.78) 100%); }
.hero-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 32rpx 36rpx 64rpx; color: #fff; }
.hero-en { font-size: 20rpx; letter-spacing: 4rpx; color: #E7D3AE; }
.hname { font-size: 44rpx; font-weight: 500; margin-top: 10rpx; line-height: 1.3; }
.hsub { font-size: 24rpx; color: rgba(255,255,255,.82); margin-top: 10rpx; line-height: 1.55; }
.hprice { margin-top: 16rpx; font-size: 36rpx; color: #F6E7C8; }
.sheet { margin-top: -32rpx; position: relative; }
.ft { font-size: 30rpx; font-weight: 500; margin: 20rpx 0 12rpx; }
.body { font-size: 26rpx; line-height: 1.85; color: #4A433C; }
.consult { display: flex; gap: 20rpx; padding: 16rpx; }
.consult-img { width: 180rpx; height: 180rpx; border-radius: 18rpx; flex-shrink: 0; }
.consult-body { flex: 1; padding: 8rpx 8rpx 0 0; }
.plabel { font-size: 20rpx; color: #8A8175; letter-spacing: 2rpx; }
.pamount { margin-right: 0; font-size: 36rpx; }
</style>
