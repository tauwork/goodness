<template>
  <view class="page sink" v-if="p">
    <view class="hero">
      <image class="hero-img" :src="image" mode="aspectFill" />
      <view class="hero-shade"></view>
      <view class="hero-copy">
        <text class="hero-en">{{ en }}</text>
        <view class="hero-name">{{ p.name }}</view>
        <view class="hero-sub">{{ p.subtitle }}</view>
      </view>
    </view>

    <view class="card sheet">
      <view class="attrs" v-if="attrsList.length">
        <text class="tag" v-for="(v, k) in attrsList" :key="k">{{ v }}</text>
      </view>
      <view class="price" v-if="p.price != null">¥{{ money(p.price) }} <text class="unit">/ {{ unitText(p.priceUnit) }}</text></view>
    </view>

    <view class="card">
      <view class="kicker">RESERVE</view>
      <view class="form-title" v-if="isHour">开始时间</view>
      <picker v-if="isHour" mode="time" :value="form.startTime" @change="e => form.startTime = e.detail.value">
        <view class="picker">{{ form.startTime || '选择开始时间' }}</view>
      </picker>
      <view class="form-title" v-if="isHour">时长</view>
      <view class="hours" v-if="isHour">
        <view v-for="h in [1,2,3,4]" :key="h" class="hbtn" :class="{ on: form.hours === h }" @tap="form.hours = h">{{ h }} 小时</view>
      </view>
      <view class="form-title" v-else>数量 · {{ unitText(p.priceUnit) }}</view>
      <view class="hours" v-if="!isHour">
        <view v-for="q in [1,2,3]" :key="q" class="hbtn" :class="{ on: form.qty === q }" @tap="form.qty = q">{{ q }}</view>
      </view>
      <view class="form-title">预订日期</view>
      <picker mode="date" :value="form.bookDate" :start="today" @change="e => form.bookDate = e.detail.value">
        <view class="picker">{{ form.bookDate }}</view>
      </picker>
    </view>

    <view class="paybar">
      <view>
        <view class="plabel">预计费用</view>
        <view class="pamount">¥{{ amount }}</view>
      </view>
      <view class="btn-gold paybtn" @tap="submit">立即预订</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'
import { spaceImage, lineEn } from '../../utils/visual'

export default {
  data() {
    return {
      p: null,
      today: '',
      form: { startTime: '', hours: 2, qty: 1, bookDate: '' }
    }
  },
  computed: {
    isHour() { return this.p && this.p.priceUnit === 'HOUR' },
    image() { return this.p ? spaceImage(this.p) : '' },
    en() { return this.p ? lineEn(this.p.categoryCode, this.p.categoryName) : '' },
    amount() {
      if (!this.p) return 0
      const n = this.isHour ? this.form.hours : this.form.qty
      return (this.p.price * n).toFixed(2)
    },
    attrsList() {
      if (!this.p || !this.p.attrs) return []
      try {
        const a = JSON.parse(this.p.attrs)
        const label = { seats: '席', capacity: '人' }
        const facMap = { screen: '投屏', vc: '视频会议', wb: '白板', mic: '麦克风音响' }
        const out = []
        for (const [k, v] of Object.entries(a)) {
          if (k === 'facility') v.forEach(f => out.push(facMap[f] || f))
          else if (k === 'quiet') out.push(v === 's' ? '安静区' : '普通')
          else if (k === 'furnished') out.push('带家具')
          else if (k === 'mail_service') out.push('信件代收')
          else if (k === 'addr_type') out.push({ reg: '注册挂靠', vir: '虚拟办公', phys: '实体办公' }[v] || v)
          else out.push(`${v}${label[k] || ''}`)
        }
        return out
      } catch (e) { return [] }
    }
  },
  onLoad(q) {
    const d = new Date()
    this.today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    this.form.bookDate = q.date || this.today
    if (q.period === 'AM') this.form.startTime = '09:30'
    if (q.period === 'PM') this.form.startTime = '14:00'
    api.get('/api/mini/space/community').then(list => {
      const seq = list.map(c => api.get(`/api/mini/space/community/${c.id}/product`))
      return Promise.all(seq).then(arrs => arrs.flat().find(x => String(x.communityProductId) === String(q.cpid)))
    }).then(p => { if (p) this.p = p })
  },
  methods: {
    unitText(u) { return { HOUR: '小时', DAY: '天', MONTH: '月', ONCE: '场' }[u] || '次' },
    money(v) {
      const n = Number(v)
      if (Number.isNaN(n)) return v
      const s = n.toFixed(2)
      return s.endsWith('.00') ? String(Math.round(n)) : s
    },
    async submit() {
      if (this.isHour && !this.form.startTime) return uni.showToast({ title: '请选择开始时间', icon: 'none' })
      const data = await api.post('/api/mini/booking', {
        communityProductId: this.p.communityProductId,
        bookDate: this.form.bookDate,
        startTime: this.form.startTime || null,
        hours: this.isHour ? this.form.hours : null,
        qty: this.isHour ? null : this.form.qty
      })
      await api.post(`/api/mini/booking/mock-pay/${data.orderNo}`)
      uni.showModal({
        title: '预订成功',
        content: `订单 ${data.orderNo} 已支付 ¥${data.amount}，入场码已生成`,
        confirmText: '查看预订',
        success: r => r.confirm && uni.redirectTo({ url: '/pages/me/bookings' })
      })
    }
  }
}
</script>

<style scoped>
.hero { position: relative; height: 520rpx; }
.hero-img, .hero-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.hero-shade { background: linear-gradient(180deg, rgba(16,14,12,0) 35%, rgba(16,14,12,.72) 100%); }
.hero-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 32rpx 36rpx 56rpx; color: #fff; }
.hero-en { font-size: 20rpx; letter-spacing: 4rpx; color: #E7D3AE; }
.hero-name { font-size: 44rpx; font-weight: 500; margin-top: 8rpx; }
.hero-sub { font-size: 24rpx; color: rgba(255,255,255,.82); margin-top: 8rpx; line-height: 1.5; }
.sheet { margin-top: -28rpx; position: relative; }
.attrs { margin-bottom: 12rpx; }
.price { color: #8C6A3D; font-size: 40rpx; font-weight: 600; }
.unit { font-size: 22rpx; font-weight: 400; }
.form-title { font-size: 26rpx; margin: 28rpx 0 14rpx; color: #1C1915; }
.picker { background: #F3EEE6; border-radius: 16rpx; padding: 22rpx 24rpx; font-size: 28rpx; }
.hours { display: flex; flex-wrap: wrap; gap: 16rpx; }
.hbtn { background: #F3EEE6; border-radius: 999rpx; padding: 16rpx 32rpx; font-size: 26rpx; color: #6F6252; }
.hbtn.on { background: #1C1915; color: #F6E7C8; }
.plabel { font-size: 20rpx; color: #8A8175; letter-spacing: 2rpx; }
.pamount { margin-right: 0; }
</style>
