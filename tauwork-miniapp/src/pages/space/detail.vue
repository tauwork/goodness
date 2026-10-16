<template>
  <view class="page" v-if="p">
    <view class="card">
      <view class="name">{{ p.name }} <text class="tag">{{ p.categoryName }}</text></view>
      <view class="sub" style="margin:8rpx 0 16rpx">{{ p.subtitle }}</view>
      <view class="attrs">
        <text class="tag" v-for="(v, k) in attrsList" :key="k">{{ v }}</text>
      </view>
      <view class="price">¥{{ p.price }} <text class="unit">/{{ unitText(p.priceUnit) }}</text></view>
    </view>

    <!-- 预订信息 -->
    <view class="card">
      <view class="form-title" v-if="isHour">开始时间</view>
      <picker v-if="isHour" mode="time" :value="form.startTime" @change="e => form.startTime = e.detail.value">
        <view class="picker">{{ form.startTime || '选择开始时间' }}</view>
      </picker>
      <view class="form-title" v-if="isHour">时长（小时）</view>
      <view class="hours" v-if="isHour">
        <view v-for="h in [1,2,3,4]" :key="h" class="hbtn" :class="{ on: form.hours === h }" @tap="form.hours = h">{{ h }}h</view>
      </view>
      <view class="form-title" v-else>数量（{{ unitText(p.priceUnit) }}）</view>
      <view class="hours" v-if="!isHour">
        <view v-for="q in [1,2,3]" :key="q" class="hbtn" :class="{ on: form.qty === q }" @tap="form.qty = q">{{ q }}</view>
      </view>
      <view class="form-title">预订日期</view>
      <picker mode="date" :value="form.bookDate" :start="today" @change="e => form.bookDate = e.detail.value">
        <view class="picker">{{ form.bookDate }}</view>
      </picker>
    </view>

    <view class="amount card">
      <text>预计费用</text>
      <text class="price">¥{{ amount }}</text>
    </view>

    <view class="paybar">
      <view class="pamount">¥{{ amount }}</view>
      <view class="btn-gold paybtn" @tap="submit">立即预订</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

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
    this.form.bookDate = this.today
    // 从列表页带过来的产品对象或重新拉取
    const ev = this.getOpenerEventChannel && this.getOpenerEventChannel()
    api.get('/api/mini/space/community').then(list => {
      const seq = list.map(c => api.get(`/api/mini/space/community/${c.id}/product`))
      return Promise.all(seq).then(arrs => arrs.flat().find(x => x.communityProductId === +q.cpid))
    }).then(p => { if (p) this.p = p })
  },
  methods: {
    unitText(u) { return { HOUR: '小时', DAY: '天', MONTH: '月', ONCE: '场' }[u] || '次' },
    async submit() {
      if (this.isHour && !this.form.startTime) return uni.showToast({ title: '请选择开始时间', icon: 'none' })
      const data = await api.post('/api/mini/booking', {
        communityProductId: this.p.communityProductId,
        bookDate: this.form.bookDate,
        startTime: this.form.startTime || null,
        hours: this.isHour ? this.form.hours : null,
        qty: this.isHour ? null : this.form.qty
      })
      // mock 支付（正式环境 wx.requestOrderPayment(data.payParams)）
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
.name { font-size: 34rpx; font-weight: 600; }
.attrs { margin: 10rpx 0; }
.price { color: #A8853F; font-size: 36rpx; font-weight: 700; margin-top: 10rpx; }
.unit { font-size: 22rpx; font-weight: 400; }
.form-title { font-size: 26rpx; font-weight: 600; margin: 24rpx 0 14rpx; }
.picker { background: #F4F6FA; border-radius: 14rpx; padding: 20rpx 24rpx; color: #22304A; font-size: 26rpx; }
.hours { display: flex; gap: 16rpx; }
.hbtn { border: 1rpx solid #E8ECF3; background: #fff; border-radius: 14rpx; padding: 14rpx 40rpx; font-size: 26rpx; }
.hbtn.on { background: #16355E; color: #fff; border-color: #16355E; }
.amount { display: flex; justify-content: space-between; align-items: center; font-size: 26rpx; color: #8A94A8; }
.paybar { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; border-top: 1rpx solid #E8ECF3;
  display: flex; align-items: center; padding: 20rpx 28rpx calc(20rpx + env(safe-area-inset-bottom)); }
.pamount { color: #A8853F; font-size: 36rpx; font-weight: 700; margin-right: 24rpx; }
.paybtn { flex: 1; }
</style>
