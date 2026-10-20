<template>
  <view class="page sink" v-if="p">
    <image class="hero" :src="img(p.coverUrl)" mode="aspectFill" />
    <view class="card sheet">
      <text class="kicker">ATELIER</text>
      <view class="name">{{ p.name }}</view>
      <view class="sub">{{ p.subtitle }}</view>
      <view class="price">¥{{ p.priceValue }}<text class="spec" v-if="p.spec">  ·  {{ p.spec }}</text></view>
      <view class="sub" style="margin-top:8rpx">库存 {{ p.stock }}</view>
    </view>
    <view class="card" v-if="p.detail">
      <view class="kicker">NOTES</view>
      <view class="ft">详情</view>
      <view class="body">{{ p.detail }}</view>
    </view>
    <view class="card">
      <view class="row">
        <text>数量</text>
        <view class="step">
          <text class="op" @tap="qty = Math.max(1, qty - 1)">－</text>
          <text class="qn">{{ qty }}</text>
          <text class="op" @tap="qty = Math.min(p.stock || 1, qty + 1)">＋</text>
        </view>
      </view>
      <input class="remark" v-model="remark" placeholder="备注，例如到店自取" />
    </view>
    <view class="paybar">
      <view>
        <view class="plabel">合计</view>
        <view class="pamount">¥{{ amount }}</view>
      </view>
      <view v-if="p.stock > 0" class="btn-gold paybtn" @tap="buy">立即下单</view>
      <view v-else class="btn-main paybtn off">已售罄</view>
    </view>
  </view>
</template>

<script>
import api, { BASE } from '../../utils/request'
import { visual, mediaUrl } from '../../utils/visual'

export default {
  data() {
    return { p: null, qty: 1, remark: '' }
  },
  computed: {
    amount() {
      if (!this.p || this.p.priceValue == null) return ''
      return (Number(this.p.priceValue) * this.qty).toFixed(2)
    }
  },
  onLoad(q) {
    api.get(`/api/mini/goods/product/${q.id}`).then(p => this.p = p)
  },
  methods: {
    img(url) { return url ? mediaUrl(url, BASE) : visual.gift },
    buy() {
      api.post('/api/mini/goods-order', { productId: this.p.id, qty: this.qty, remark: this.remark || undefined }, true)
        .then(order => {
          if (!order.mock) {
            uni.showToast({ title: '请在微信中完成支付', icon: 'none' })
            return
          }
          uni.showModal({
            title: '确认支付',
            content: `${order.orderNo}  ¥${order.amount}`,
            success: res => {
              if (!res.confirm) {
                uni.navigateTo({ url: '/pages/me/bookings?tab=goods' })
                return
              }
              api.post(`/api/mini/goods-order/mock-pay/${order.orderNo}`, {}, true).then(() => {
                uni.showToast({ title: '支付成功', icon: 'none' })
                setTimeout(() => uni.navigateTo({ url: '/pages/me/bookings?tab=goods' }), 400)
              })
            }
          })
        })
    }
  }
}
</script>

<style scoped>
.hero { width: 100%; height: 640rpx; display: block; }
.sheet { margin-top: -36rpx; position: relative; }
.name { font-size: 40rpx; font-weight: 500; margin-top: 10rpx; }
.price { color: #8C6A3D; font-size: 36rpx; font-weight: 600; margin-top: 14rpx; }
.spec { font-size: 24rpx; font-weight: 400; color: #8A8175; }
.ft { font-size: 30rpx; margin: 12rpx 0; }
.body { line-height: 1.8; color: #4A433C; font-size: 26rpx; }
.row { display: flex; align-items: center; justify-content: space-between; margin: 8rpx 0 24rpx; }
.step { display: flex; align-items: center; gap: 28rpx; }
.op { width: 64rpx; height: 64rpx; line-height: 64rpx; text-align: center; background: #F3EEE6; border-radius: 50%; font-size: 32rpx; }
.qn { min-width: 32rpx; text-align: center; font-size: 30rpx; }
.remark { background: #F3EEE6; border-radius: 16rpx; padding: 22rpx 24rpx; font-size: 26rpx; }
.plabel { font-size: 20rpx; color: #8A8175; letter-spacing: 2rpx; }
.pamount { margin-right: 0; }
.off { opacity: .45; }
</style>
