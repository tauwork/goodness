<template>
  <view class="page">
    <view class="card" style="margin-top:24rpx">
      <view class="ft">香港身份智能评估</view>
      <view class="sub">3分钟 · 完成后自动生成专属路径建议</view>
      <view class="progress">
        <view class="pbar"><view class="pfill" :style="{ width: progress + '%' }"></view></view>
        <text class="ptext">{{ doneCount }}/{{ questions.length }}</text>
      </view>
    </view>

    <view class="card" v-for="q in questions" :key="q.id">
      <view class="qt">{{ q.title }} <text v-if="!q.multi" class="req">*</text><text v-if="q.multi" class="opt-tag">可多选</text></view>
      <view class="chips">
        <view v-for="o in q.options" :key="o.id" class="chip"
              :class="{ on: selected[q.code] && selected[q.code].includes(o.id) }"
              @tap="pick(q, o.id)">{{ o.label }}</view>
      </view>
    </view>

    <view class="btn-gold" :class="{ dis: !canSubmit }" style="margin:24rpx" @tap="submit">生成评估报告</view>
    <view style="height:40rpx"></view>
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
.ft { font-size: 32rpx; font-weight: 600; }
.progress { display: flex; align-items: center; gap: 20rpx; margin-top: 20rpx; }
.pbar { flex: 1; height: 10rpx; background: #EEF1F6; border-radius: 6rpx; overflow: hidden; }
.pfill { height: 100%; background: linear-gradient(90deg,#C2A05F,#A8853F); border-radius: 6rpx; }
.ptext { font-size: 22rpx; color: #A8853F; font-weight: 600; }
.qt { font-size: 28rpx; font-weight: 600; margin-bottom: 20rpx; }
.req { color: #D9534F; }
.opt-tag { font-size: 20rpx; color: #A8853F; background: #F7F1E2; border-radius: 8rpx; padding: 2rpx 12rpx; margin-left: 12rpx; font-weight: 400; }
.chips { display: flex; flex-wrap: wrap; gap: 16rpx; }
.chip { border: 1rpx solid #E8ECF3; background: #fff; border-radius: 30rpx; padding: 16rpx 34rpx; font-size: 26rpx; }
.chip.on { background: #16355E; color: #fff; border-color: #16355E; font-weight: 500; }
.dis { opacity: .45; }
</style>
