<template>
  <view class="page" v-if="r">
    <view class="ring-wrap">
      <view class="ring">
        <view class="rin">
          <text class="rscore">{{ r.score }}</text>
          <text class="rlabel">综合适配度</text>
        </view>
      </view>
    </view>

    <view class="card rec" v-for="(rec, i) in r.recommends" :key="rec.code" @tap="go(rec)">
      <view class="rh">
        <text v-if="i === 0" class="best">最优推荐</text>
        <text class="rname">{{ rec.name }}</text>
        <text class="rfit">{{ rec.fit }}%</text>
      </view>
      <text class="go">查看服务详情并预约顾问解读 ›</text>
    </view>

    <view class="card">
      <view class="ft">下一步建议</view>
      <view class="line">· 预约顾问一对一解读报告（首次免费）</view>
      <view class="line">· 同步评估孩子入学路径与插班时间窗</view>
      <view class="line">· 完善家庭档案，获取更精准的路径方案</view>
    </view>
    <view style="height:40rpx"></view>
  </view>
</template>

<script>
export default {
  data() {
    return { r: null }
  },
  onShow() {
    const raw = uni.getStorageSync('assessResult')
    if (raw) this.r = JSON.parse(raw)
  },
  methods: {
    go(rec) {
      if (rec.productId) uni.navigateTo({ url: `/pages/svc/detail?id=${rec.productId}` })
    }
  }
}
</script>

<style scoped>
.ring-wrap { display: flex; justify-content: center; padding: 48rpx 0 32rpx; }
.ring { width: 236rpx; height: 236rpx; border-radius: 50%;
  background: conic-gradient(#C2A05F 0deg, #C2A05F calc(var(--p, 100% * 1deg)), #E9EDF4 0deg);
  display: flex; align-items: center; justify-content: center; }
.rin { width: 184rpx; height: 184rpx; border-radius: 50%; background: #fff;
  display: flex; flex-direction: column; align-items: center; justify-content: center; }
.rscore { font-size: 60rpx; font-weight: 700; color: #16355E; }
.rlabel { font-size: 20rpx; color: #8A94A8; }
.rec { border: 2rpx solid #C2A05F; background: #FFFDF7; }
.rh { display: flex; align-items: center; gap: 14rpx; }
.best { font-size: 20rpx; color: #A8853F; background: #C2A05F; border-radius: 8rpx; padding: 4rpx 14rpx; color: #fff; }
.rname { flex: 1; font-size: 30rpx; font-weight: 600; }
.rfit { color: #A8853F; font-weight: 700; font-size: 28rpx; }
.go { display: block; margin-top: 14rpx; font-size: 22rpx; color: #16355E; }
.ft { font-size: 28rpx; font-weight: 600; margin-bottom: 14rpx; }
.line { font-size: 24rpx; color: #3A4658; line-height: 2; }
</style>
