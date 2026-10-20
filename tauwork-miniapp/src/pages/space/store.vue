<template>
  <view class="page" v-if="current">
    <view class="store">
      <image class="store-img" :src="storeImage" mode="aspectFill" />
      <view class="store-shade"></view>
      <view class="store-copy">
        <view class="store-name">{{ current.name }}</view>
        <view class="store-meta">{{ current.address }}</view>
        <view class="store-meta">{{ current.openHours }}</view>
        <view class="when" v-if="bookDate">{{ dateLabel }} · {{ periodLabel }}</view>
      </view>
    </view>

    <view class="block" v-if="products.length">
      <text class="kicker">AVAILABLE</text>
      <text class="block-title">可预订</text>
    </view>
    <view class="scard" v-for="p in products" :key="p.communityProductId" @tap="go(p)">
      <image class="simg" :src="p.image" mode="aspectFill" />
      <view class="sbody">
        <text class="kicker">{{ p.en }}</text>
        <view class="sname">{{ p.name }}</view>
        <view class="ssub">{{ p.subtitle }}</view>
        <view class="sfoot">
          <text class="sprice">¥{{ p.price }}<text class="sunit"> / {{ unitText(p.priceUnit) }}</text></text>
          <text class="sstock" v-if="p.stock !== null">余 {{ p.stock }}</text>
        </view>
      </view>
    </view>
    <view v-if="loaded && !products.length" class="empty-box">
      <image src="/static/visual/desk.jpg" mode="aspectFill" />
      <text>该社区暂无可预订产品</text>
    </view>
  </view>
</template>

<script>
import api, { BASE } from '../../utils/request'
import { communityImage, spaceImage, lineEn, mediaUrl } from '../../utils/visual'

export default {
  data() {
    return { current: null, products: [], loaded: false, bookDate: '', period: 'ALL' }
  },
  computed: {
    storeImage() {
      if (!this.current) return ''
      return this.current.coverUrl ? mediaUrl(this.current.coverUrl, BASE) : communityImage(this.current.name)
    },
    dateLabel() {
      if (!this.bookDate) return ''
      const [, m, d] = this.bookDate.split('-')
      return `${Number(m)}月${Number(d)}日`
    },
    periodLabel() {
      return { ALL: '全天', AM: '上午', PM: '下午' }[this.period] || ''
    }
  },
  onLoad(q) {
    this.bookDate = q.date || ''
    this.period = q.period || 'ALL'
    api.get(`/api/mini/space/community/${q.cid}`).then(c => { this.current = c })
    api.get(`/api/mini/space/community/${q.cid}/product`).then(d => {
      this.products = (d || []).map(p => ({
        ...p,
        image: spaceImage(p),
        en: lineEn(p.categoryCode, p.categoryName)
      }))
    }).finally(() => { this.loaded = true })
  },
  methods: {
    unitText(u) {
      return { HOUR: '小时', DAY: '天', MONTH: '月', ONCE: '场' }[u] || '次'
    },
    go(p) {
      const q = [`cpid=${p.communityProductId}`]
      if (this.bookDate) q.push(`date=${this.bookDate}`)
      if (this.period) q.push(`period=${this.period}`)
      uni.navigateTo({ url: `/pages/space/detail?${q.join('&')}` })
    }
  }
}
</script>

<style scoped>
.store { position: relative; height: 420rpx; margin: 20rpx 28rpx 8rpx; border-radius: 28rpx; overflow: hidden; }
.store-img, .store-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.store-shade { background: linear-gradient(180deg, rgba(16,14,12,0) 30%, rgba(16,14,12,.72) 100%); }
.store-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 28rpx 32rpx; color: #fff; }
.store-name { font-size: 36rpx; font-weight: 500; }
.store-meta, .when { font-size: 22rpx; color: rgba(255,255,255,.84); margin-top: 6rpx; }
.block { padding: 28rpx 36rpx 12rpx; }
.block-title { display: block; font-size: 36rpx; font-weight: 500; margin-top: 6rpx; }
.scard { margin: 0 28rpx 24rpx; background: #FFFcf8; border-radius: 28rpx; overflow: hidden; border: 1rpx solid rgba(28,25,21,.06); }
.simg { width: 100%; height: 300rpx; display: block; }
.sbody { padding: 24rpx 28rpx 28rpx; }
.sname { font-size: 32rpx; font-weight: 500; margin-top: 8rpx; }
.ssub { color: #8A8175; font-size: 24rpx; line-height: 1.6; margin-top: 8rpx; }
.sfoot { display: flex; align-items: baseline; margin-top: 16rpx; }
.sprice { color: #8C6A3D; font-size: 34rpx; font-weight: 600; }
.sunit { font-size: 22rpx; font-weight: 400; }
.sstock { margin-left: 16rpx; color: #6E8B74; font-size: 22rpx; }
</style>
