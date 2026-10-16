<template>
  <view class="page" v-if="p">
    <view class="card">
      <view class="name">{{ p.name }}</view>
      <view class="sub" style="margin:8rpx 0">{{ p.subtitle }}</view>
      <view class="price">¥{{ p.priceValue }}<text v-if="p.priceMode==='STAGED'"> 起</text></view>
    </view>

    <view class="card">
      <view class="ft">咨询主题</view>
      <input class="inp" v-model="form.topic" disabled placeholder="咨询主题" />
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
      <view class="ft">补充说明（选填）</view>
      <textarea class="inp ta" v-model="form.remark" maxlength="200" placeholder="简单描述您的情况，帮助顾问提前准备" />
    </view>

    <view class="paybar">
      <view class="free">首次评估咨询<span class="sub">免费</span></view>
      <view class="btn-gold paybtn" @tap="submit">提交预约</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    const d = new Date()
    const today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    return {
      p: null, today, slots: ['09:30', '10:30', '14:00', '15:30', '16:30', '19:00'],
      modes: [
        { v: 'VIDEO', label: '视频咨询' },
        { v: 'FUTIAN', label: '深圳·福田' },
        { v: 'BAOAN', label: '深圳·宝安' },
        { v: 'KWAI_TSING', label: '香港·葵青' }
      ],
      form: { mode: 'VIDEO', bookDate: today, timeSlot: '', remark: '' }
    }
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
        title: '预约提交成功',
        content: '顾问将在 1 小时内确认，请留意「我的预约」状态',
        confirmText: '查看预约',
        success: r => r.confirm ? uni.redirectTo({ url: '/pages/me/bookings?tab=svc' }) : uni.navigateBack()
      })
    }
  }
}
</script>

<style scoped>
.name { font-size: 34rpx; font-weight: 600; }
.price { color: #A8853F; font-size: 36rpx; font-weight: 700; margin-top: 10rpx; }
.ft { font-size: 26rpx; font-weight: 600; margin: 26rpx 0 14rpx; }
.inp { background: #F4F6FA; border-radius: 14rpx; padding: 20rpx 24rpx; font-size: 26rpx; width: 100%; box-sizing: border-box; }
.ta { min-height: 140rpx; }
.picker { background: #F4F6FA; border-radius: 14rpx; padding: 20rpx 24rpx; font-size: 26rpx; }
.chips { display: flex; flex-wrap: wrap; gap: 14rpx; }
.chip { border: 1rpx solid #E8ECF3; background: #fff; border-radius: 28rpx; padding: 12rpx 30rpx; font-size: 24rpx; }
.chip.on { background: #16355E; color: #fff; border-color: #16355E; }
.paybar { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; border-top: 1rpx solid #E8ECF3;
  display: flex; align-items: center; padding: 20rpx 28rpx calc(20rpx + env(safe-area-inset-bottom)); }
.free { font-size: 30rpx; font-weight: 600; margin-right: 24rpx; }
.free .sub { margin-left: 10rpx; color: #A8853F; }
.paybtn { flex: 1; }
</style>
