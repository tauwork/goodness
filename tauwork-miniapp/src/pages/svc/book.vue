<template>
  <view class="page sink" v-if="p">
    <view class="brief">
      <image class="brief-img" :src="image" mode="aspectFill" />
      <view class="brief-body">
        <text class="kicker">{{ en }}</text>
        <view class="name">{{ p.name }}</view>
        <view class="sub">{{ p.subtitle }}</view>
      </view>
    </view>

    <view class="card">
      <view class="kicker">APPOINTMENT</view>
      <view class="ft">咨询方式</view>
      <view class="chips">
        <view v-for="m in modes" :key="m.v" class="chip" :class="{ on: form.mode === m.v }"
              @tap="form.mode = m.v">{{ m.label }}</view>
      </view>
      <view class="ft">预约日期</view>
      <picker mode="date" :value="form.bookDate" :start="today" @change="e => form.bookDate = e.detail.value">
        <view class="picker">{{ form.bookDate }}</view>
      </picker>
      <view class="ft">预约时段</view>
      <view class="chips">
        <view v-for="s in slots" :key="s" class="chip" :class="{ on: form.timeSlot === s }"
              @tap="form.timeSlot = s">{{ s }}</view>
      </view>
      <view class="ft">补充说明</view>
      <textarea class="inp ta" v-model="form.remark" maxlength="200" placeholder="简单描述您的情况，方便顾问提前准备" />
    </view>

    <view class="paybar">
      <view>
        <view class="plabel">首次评估咨询</view>
        <view class="pamount">免费</view>
      </view>
      <view class="btn-gold paybtn" @tap="submit">提交预约</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'
import { serviceImage, lineEn } from '../../utils/visual'

export default {
  data() {
    const d = new Date()
    const today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    return {
      p: null, today, slots: ['09:30', '10:30', '14:00', '15:30', '16:30', '19:00'],
      modes: [
        { v: 'VIDEO', label: '视频咨询' },
        { v: 'FUTIAN', label: '深圳 · 福田' },
        { v: 'BAOAN', label: '深圳 · 宝安' },
        { v: 'KWAI_TSING', label: '香港 · 葵青' }
      ],
      form: { mode: 'VIDEO', bookDate: today, timeSlot: '', remark: '' }
    }
  },
  computed: {
    image() { return this.p ? serviceImage(this.p) : '' },
    en() { return this.p ? lineEn(this.p.bizLine, this.p.name) : '' }
  },
  onLoad(q) {
    api.get(`/api/mini/svc/product/${q.id}`).then(p => this.p = p)
  },
  methods: {
    async submit() {
      if (!this.form.timeSlot) return uni.showToast({ title: '请选择时段', icon: 'none' })
      await api.post('/api/mini/svc-booking', {
        productId: this.p.id,
        topic: this.p.name,
        mode: this.form.mode,
        bookDate: this.form.bookDate,
        timeSlot: this.form.timeSlot,
        remark: this.form.remark || null
      })
      uni.showModal({
        title: '预约已提交',
        content: '顾问将在 1 小时内确认，请留意「我的预订」',
        confirmText: '查看预约',
        success: r => r.confirm ? uni.redirectTo({ url: '/pages/me/bookings?tab=svc' }) : uni.navigateBack()
      })
    }
  }
}
</script>

<style scoped>
.brief { display: flex; gap: 20rpx; margin: 24rpx 28rpx; background: #FFFcf8; border-radius: 24rpx; overflow: hidden; border: 1rpx solid rgba(28,25,21,.06); }
.brief-img { width: 200rpx; height: 200rpx; flex-shrink: 0; }
.brief-body { flex: 1; padding: 24rpx 24rpx 20rpx 0; }
.name { font-size: 32rpx; font-weight: 500; margin-top: 8rpx; line-height: 1.35; }
.ft { font-size: 26rpx; margin: 28rpx 0 14rpx; }
.inp { background: #F3EEE6; border-radius: 16rpx; padding: 22rpx 24rpx; font-size: 26rpx; width: 100%; box-sizing: border-box; }
.ta { min-height: 160rpx; }
.picker { background: #F3EEE6; border-radius: 16rpx; padding: 22rpx 24rpx; font-size: 28rpx; }
.chips { display: flex; flex-wrap: wrap; gap: 14rpx; }
.chip { background: #F3EEE6; border-radius: 999rpx; padding: 14rpx 28rpx; font-size: 24rpx; color: #6F6252; }
.chip.on { background: #1C1915; color: #F6E7C8; }
.plabel { font-size: 20rpx; color: #8A8175; letter-spacing: 2rpx; }
.pamount { margin-right: 0; font-size: 36rpx; }
</style>
