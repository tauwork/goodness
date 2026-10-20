<template>
  <view class="page">
    <view class="hero">
      <image class="hero-img" src="/static/visual/identity.jpg" mode="aspectFill" />
      <view class="hero-shade"></view>
      <view class="hero-copy">
        <text class="hero-en">ASSESSMENT</text>
        <view class="hero-title">香港身份评估</view>
        <view class="hero-sub">大约三分钟。完成后给出一条更适合你的路径。</view>
      </view>
    </view>

    <view class="card sheet">
      <view class="progress">
        <view class="pbar"><view class="pfill" :style="{ width: progress + '%' }"></view></view>
        <text class="ptext">{{ doneCount }} / {{ questions.length }}</text>
      </view>
    </view>

    <view class="card" v-for="(q, i) in questions" :key="q.id">
      <text class="kicker">{{ pad(i + 1) }}</text>
      <view class="qt">{{ q.title }} <text v-if="!q.multi" class="req">*</text><text v-if="q.multi" class="opt-tag">可多选</text></view>
      <view class="chips">
        <view v-for="o in q.options" :key="o.id" class="chip"
              :class="{ on: selected[q.code] && selected[q.code].includes(o.id) }"
              @tap="pick(q, o.id)">{{ o.label }}</view>
      </view>
    </view>

    <view class="btn-gold submit" :class="{ dis: !canSubmit }" @tap="submit">生成评估报告</view>
    <view style="height: 40rpx"></view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { questions: [], selected: {}, totalWeight: 0 }
  },
  computed: {
    doneCount() {
      return this.questions.filter(q => (this.selected[q.code] || []).length > 0).length
    },
    progress() {
      return this.questions.length ? Math.round(this.doneCount / this.questions.length * 100) : 0
    },
    canSubmit() {
      return this.questions.filter(q => !q.multi).every(q => (this.selected[q.code] || []).length > 0)
    }
  },
  onShow() {
    api.get('/api/mini/assess/quiz').then(d => {
      this.questions = d.questions
      this.totalWeight = d.totalWeight
    })
  },
  methods: {
    pad(n) { return String(n).padStart(2, '0') },
    pick(q, oid) {
      const cur = this.selected[q.code] || []
      if (q.multi) {
        const i = cur.indexOf(oid)
        this.selected[q.code] = i >= 0 ? cur.filter(x => x !== oid) : [...cur, oid]
      } else {
        this.selected[q.code] = [oid]
      }
    },
    async submit() {
      if (!this.canSubmit) return uni.showToast({ title: '请完成必答题目', icon: 'none' })
      const r = await api.post('/api/mini/assess/submit', this.selected)
      uni.setStorageSync('assessResult', JSON.stringify(r))
      uni.navigateTo({ url: '/pages/assess/result' })
    }
  }
}
</script>

<style scoped>
.hero { position: relative; height: 360rpx; }
.hero-img, .hero-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.hero-shade { background: linear-gradient(180deg, rgba(16,14,12,.1), rgba(16,14,12,.7)); }
.hero-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 32rpx 36rpx 56rpx; color: #fff; }
.hero-en { font-size: 20rpx; letter-spacing: 4rpx; color: #E7D3AE; }
.hero-title { font-size: 44rpx; font-weight: 500; margin-top: 8rpx; }
.hero-sub { font-size: 24rpx; color: rgba(255,255,255,.82); margin-top: 8rpx; }
.sheet { margin-top: -28rpx; position: relative; }
.progress { display: flex; align-items: center; gap: 20rpx; }
.pbar { flex: 1; height: 8rpx; background: #E8E2D8; border-radius: 8rpx; overflow: hidden; }
.pfill { height: 100%; background: #1C1915; border-radius: 8rpx; }
.ptext { font-size: 22rpx; color: #8C6A3D; }
.qt { font-size: 30rpx; font-weight: 500; margin: 12rpx 0 20rpx; line-height: 1.45; }
.req { color: #8C4A42; }
.opt-tag { font-size: 20rpx; color: #8C6A3D; background: #F6EEDD; border-radius: 999rpx; padding: 4rpx 14rpx; margin-left: 12rpx; font-weight: 400; }
.chips { display: flex; flex-wrap: wrap; gap: 16rpx; }
.chip { background: #F3EEE6; border-radius: 999rpx; padding: 16rpx 32rpx; font-size: 26rpx; color: #4A433C; }
.chip.on { background: #1C1915; color: #F6E7C8; }
.submit { margin: 12rpx 28rpx 0; }
.dis { opacity: .4; }
</style>
