<template>
  <view class="page">
    <view class="intro">
      <text class="kicker">ADVISORY</text>
      <view class="intro-title">把下一步讲清楚</view>
      <view class="intro-desc">身份、公司、教育与安居。顾问按你的处境来，不按套餐推销。</view>
    </view>

    <scroll-view scroll-x class="cats" :show-scrollbar="false">
      <view class="cat" :class="{ on: !catId }" @tap="pick(null)">
        <image src="/static/visual/harbor.jpg" mode="aspectFill" />
        <view class="cat-tx"><text class="cat-en">ALL</text><text class="cat-cn">全部服务</text></view>
      </view>
      <view class="cat" v-for="c in cats" :key="c.id" :class="{ on: catId === c.id }" @tap="pick(c.id)">
        <image :src="c.image" mode="aspectFill" />
        <view class="cat-tx"><text class="cat-en">{{ c.en }}</text><text class="cat-cn">{{ c.name }}</text></view>
      </view>
    </scroll-view>

    <view class="item" v-for="p in list" :key="p.id" @tap="go(p.id)">
      <image class="cover" :src="p.image" mode="aspectFill" />
      <view class="info">
        <text class="kicker">{{ p.en }}</text>
        <view class="name">{{ p.name }}</view>
        <view class="sub line">{{ p.subtitle }}</view>
        <view class="price">¥{{ p.priceValue }}<text v-if="p.priceMode==='STAGED'" class="from"> 起</text></view>
      </view>
    </view>
    <view v-if="loaded && !list.length" class="empty-box">
      <image src="/static/visual/company.jpg" mode="aspectFill" />
      <text>这一类暂无在售服务</text>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'
import { serviceImage, lineEn } from '../../utils/visual'

export default {
  data() {
    return { cats: [], list: [], catId: null, loaded: false }
  },
  onShow() {
    api.get('/api/mini/svc/category').then(d => {
      this.cats = (d || []).map(c => ({ ...c, image: serviceImage(c), en: lineEn(c.code, c.name) }))
    })
    this.load()
  },
  methods: {
    pick(id) {
      this.catId = id
      this.load()
    },
    load() {
      this.loaded = false
      api.get('/api/mini/svc/product', { categoryId: this.catId || undefined }).then(d => {
        this.list = (d || []).map(p => ({ ...p, image: serviceImage(p), en: lineEn(p.bizLine, p.name) }))
      }).finally(() => { this.loaded = true })
    },
    go(id) { uni.navigateTo({ url: `/pages/svc/detail?id=${id}` }) }
  }
}
</script>

<style scoped>
.intro { padding: 28rpx 36rpx 12rpx; }
.intro-title { font-size: 44rpx; font-weight: 500; margin-top: 8rpx; }
.intro-desc { color: #8A8175; font-size: 26rpx; line-height: 1.6; margin-top: 10rpx; }
.cats { white-space: nowrap; padding: 16rpx 0 8rpx 28rpx; height: 200rpx; }
.cat {
  display: inline-block; position: relative; width: 280rpx; height: 176rpx;
  margin-right: 16rpx; border-radius: 22rpx; overflow: hidden; vertical-align: top;
  border: 4rpx solid transparent; box-sizing: border-box;
}
.cat.on { border-color: #1C1915; }
.cat image { width: 100%; height: 100%; display: block; }
.cat-tx { position: absolute; left: 0; right: 0; bottom: 0; padding: 28rpx 18rpx 14rpx; background: linear-gradient(transparent, rgba(16,14,12,.75)); }
.cat-en { display: block; color: #E7D3AE; font-size: 16rpx; letter-spacing: 2rpx; }
.cat-cn { display: block; color: #fff; font-size: 26rpx; margin-top: 2rpx; }
.item {
  display: flex; gap: 20rpx; margin: 20rpx 28rpx 0; padding: 16rpx;
  background: #FFFcf8; border-radius: 24rpx; border: 1rpx solid rgba(28,25,21,.06);
}
.cover { width: 188rpx; height: 188rpx; border-radius: 18rpx; flex-shrink: 0; }
.info { flex: 1; min-width: 0; padding: 6rpx 8rpx 0 0; }
.name { font-size: 30rpx; font-weight: 500; margin-top: 6rpx; line-height: 1.35; }
.line { margin-top: 8rpx; overflow: hidden; height: 72rpx; }
.price { color: #8C6A3D; font-size: 30rpx; font-weight: 600; margin-top: 8rpx; }
.from { font-size: 22rpx; font-weight: 400; }
</style>
