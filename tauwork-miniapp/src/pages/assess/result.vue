<template>
  <view class="page" v-if="r">
    <view class="score">
      <image class="score-img" src="/static/visual/harbor.jpg" mode="aspectFill" />
      <view class="score-shade"></view>
      <view class="score-copy">
        <text class="score-en">FIT</text>
        <text class="score-num">{{ r.score }}</text>
        <text class="score-label">综合适配度</text>
      </view>
    </view>

    <view class="block">
      <text class="kicker">PATHS</text>
      <text class="block-title">建议路径</text>
    </view>
    <view class="rec" v-for="(rec, i) in r.recommends" :key="rec.code" @tap="go(rec)">
      <image class="rec-img" :src="imageOf(rec)" mode="aspectFill" />
      <view class="rec-body">
        <text v-if="i === 0" class="best">优先考虑</text>
        <view class="rname">{{ rec.name }}</view>
        <view class="rfoot">
          <text class="rfit">适配 {{ rec.fit }}%</text>
          <text class="go">查看</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="kicker">NEXT</view>
      <view class="ft">接下来可以做</view>
      <view class="line">预约顾问，一对一解读这份报告。首次免费。</view>
      <view class="line">同时看孩子的入学路径和插班时间。</view>
      <view class="line">补全家庭档案，路径会更准。</view>
    </view>
    <view style="height: 24rpx"></view>
  </view>
</template>

<script>
import { serviceImage } from '../../utils/visual'

export default {
  data() {
    return { r: null }
  },
  onShow() {
    const raw = uni.getStorageSync('assessResult')
    if (raw) this.r = JSON.parse(raw)
  },
  methods: {
    imageOf(rec) { return serviceImage(rec) },
    go(rec) {
      if (rec.productId) uni.navigateTo({ url: `/pages/svc/detail?id=${rec.productId}` })
    }
  }
}
</script>

<style scoped>
.score { position: relative; height: 460rpx; }
.score-img, .score-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.score-shade { background: linear-gradient(180deg, rgba(16,14,12,.15), rgba(16,14,12,.62)); }
.score-copy { position: absolute; left: 0; right: 0; bottom: 48rpx; text-align: center; color: #fff; }
.score-en { display: block; letter-spacing: 6rpx; font-size: 20rpx; color: #E7D3AE; }
.score-num { display: block; font-size: 96rpx; font-weight: 500; line-height: 1.1; margin-top: 8rpx; }
.score-label { display: block; font-size: 24rpx; color: rgba(255,255,255,.8); }
.block { padding: 32rpx 36rpx 12rpx; }
.block-title { display: block; font-size: 36rpx; font-weight: 500; margin-top: 6rpx; }
.rec { margin: 0 28rpx 20rpx; background: #FFFcf8; border-radius: 24rpx; overflow: hidden; border: 1rpx solid rgba(28,25,21,.06); }
.rec-img { width: 100%; height: 220rpx; display: block; }
.rec-body { padding: 22rpx 28rpx 24rpx; }
.best { font-size: 20rpx; color: #1C1915; background: #E7D3AE; border-radius: 999rpx; padding: 4rpx 14rpx; }
.rname { font-size: 32rpx; font-weight: 500; margin-top: 10rpx; }
.rfoot { display: flex; align-items: center; margin-top: 12rpx; }
.rfit { color: #8C6A3D; font-size: 26rpx; flex: 1; }
.go { color: #1C1915; font-size: 24rpx; }
.ft { font-size: 30rpx; margin: 12rpx 0 8rpx; }
.line { font-size: 26rpx; color: #4A433C; line-height: 1.7; padding: 8rpx 0; }
</style>
