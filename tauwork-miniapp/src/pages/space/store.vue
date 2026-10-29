<template>
  <view class="page" v-if="current">
    <view class="store">
      <image class="store-img" :src="storeImage" mode="aspectFill" />
      <view class="store-shade"></view>
      <view class="store-copy">
        <view class="store-name">{{ current.name }}</view>
        <view class="store-meta">{{ current.address }}</view>
        <view class="store-meta">{{ current.openHours }}</view>
      </view>
    </view>

    <view class="sheet" v-for="g in groups" :key="g.code">
      <text class="section-title">{{ g.title }}</text>
      <view class="sitem" v-for="p in g.items" :key="p.communityProductId" @tap="go(p)">
        <image class="simg" :src="p.image" mode="aspectFill" />
        <view class="sbody">
          <view class="sname">{{ p.name }}</view>
          <view class="ssub" v-if="p.subtitle">{{ p.subtitle }}</view>
          <view class="sfoot">
            <text class="sprice" v-if="p.price != null">¥{{ money(p.price) }}<text class="sunit"> / {{ unitText(p.priceUnit) }}</text></text>
            <text class="sstock" v-if="p.stock !== null">余 {{ p.stock }}</text>
          </view>
        </view>
      </view>
    </view>
    <view v-if="loaded && !products.length" class="empty">该社区暂无可预订产品</view>
  </view>
</template>

<script>
import api, { BASE } from '../../utils/request'
import { communityImage, spaceImage, mediaUrl } from '../../utils/visual'

export default {
  data() {
    return { current: null, products: [], loaded: false }
  },
  computed: {
    storeImage() {
      if (!this.current) return ''
      return this.current.coverUrl ? mediaUrl(this.current.coverUrl, BASE) : communityImage(this.current.name)
    },
    groups() {
      const order = [
        { code: 'DESK', title: '工位' },
        { code: 'MEETING', title: '会议室' }
      ]
      const shown = new Set(order.map(g => g.code))
      const sections = order.map(g => ({
        ...g,
        items: this.products.filter(p => p.categoryCode === g.code)
      })).filter(g => g.items.length)
      const rest = this.products.filter(p => !shown.has(p.categoryCode))
      if (rest.length) sections.push({ code: 'OTHER', title: '其他', items: rest })
      return sections
    }
  },
  onLoad(q) {
    api.get(`/api/mini/space/community/${q.cid}`).then(c => { this.current = c })
    api.get(`/api/mini/space/community/${q.cid}/product`).then(d => {
      this.products = (d || []).map(p => ({
        ...p,
        image: spaceImage(p)
      }))
    }).finally(() => { this.loaded = true })
  },
  methods: {
    money(v) {
      const n = Number(v)
      if (Number.isNaN(n)) return v
      const s = n.toFixed(2)
      return s.endsWith('.00') ? String(Math.round(n)) : s
    },
    unitText(u) {
      return { HOUR: '小时', DAY: '天', MONTH: '月', ONCE: '场' }[u] || '次'
    },
    go(p) {
      uni.navigateTo({ url: `/pages/space/detail?cpid=${p.communityProductId}` })
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F2F2F7; padding-bottom: 48rpx; }
.store { position: relative; height: 360rpx; margin: 16rpx 16rpx 0; border-radius: 28rpx; overflow: hidden; }
.store-img, .store-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.store-shade { background: linear-gradient(180deg, rgba(0,0,0,0) 42%, rgba(0,0,0,.55) 100%); }
.store-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 28rpx 32rpx 36rpx; color: #fff; }
.store-name { font-size: 34rpx; font-weight: 600; letter-spacing: -0.5rpx; }
.store-meta { font-size: 24rpx; color: rgba(255,255,255,.86); margin-top: 6rpx; }
.sheet {
  margin: 14rpx 16rpx 0; padding: 28rpx 0 8rpx; background: #fff; border-radius: 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(0,0,0,.04); overflow: hidden;
}
.section-title { display: block; padding: 0 28rpx 20rpx; color: #1D1D1F; font-size: 34rpx; font-weight: 600; letter-spacing: -0.5rpx; }
.sitem { display: flex; gap: 20rpx; margin: 0 24rpx 16rpx; }
.sitem + .sitem { padding-top: 16rpx; border-top: 1rpx solid #F2F2F7; }
.simg { width: 168rpx; height: 126rpx; border-radius: 16rpx; flex-shrink: 0; background: #E5E5EA; }
.sbody { flex: 1; min-width: 0; }
.sname { font-size: 28rpx; font-weight: 500; color: #1D1D1F; line-height: 1.35; }
.ssub { margin-top: 6rpx; font-size: 22rpx; color: #86868B; line-height: 1.4; }
.sfoot { display: flex; align-items: baseline; margin-top: 10rpx; }
.sprice { font-size: 28rpx; font-weight: 600; color: #1D1D1F; }
.sunit { font-size: 22rpx; font-weight: 400; color: #86868B; }
.sstock { margin-left: 16rpx; font-size: 22rpx; color: #86868B; }
.empty { text-align: center; color: #86868B; font-size: 24rpx; padding: 48rpx 0 20rpx; }
</style>
