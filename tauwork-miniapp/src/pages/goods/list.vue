<template>
  <view class="page">
    <view class="intro">
      <text class="kicker">ATELIER</text>
      <view class="intro-title">伴手礼</view>
      <view class="intro-desc">见面时带得走的一份心意，可在到店时领取。</view>
    </view>

    <scroll-view v-if="cats.length" scroll-x class="cats" :show-scrollbar="false">
      <view class="cat" :class="{ on: !catId }" @tap="pick(null)">全部</view>
      <view class="cat" v-for="c in cats" :key="c.id" :class="{ on: catId === c.id }" @tap="pick(c.id)">{{ c.name }}</view>
    </scroll-view>

    <view class="grid">
      <view class="gitem" v-for="p in list" :key="p.id" @tap="go(p.id)">
        <view class="frame">
          <image class="cover" :src="img(p)" mode="aspectFill" />
          <view v-if="p.stock === 0" class="sold">售罄</view>
        </view>
        <view class="gbody">
          <view class="name">{{ p.name }}</view>
          <view class="sub line">{{ p.subtitle }}</view>
          <view class="price">¥{{ p.priceValue }}</view>
        </view>
      </view>
    </view>
    <view v-if="loaded && !list.length" class="empty-box">
      <image src="/static/visual/gift.jpg" mode="aspectFill" />
      <text>暂无在售商品</text>
    </view>
  </view>
</template>

<script>
import api, { BASE } from '../../utils/request'
import { visual, mediaUrl } from '../../utils/visual'

export default {
  data() {
    return { cats: [], list: [], catId: null, loaded: false }
  },
  onShow() {
    api.get('/api/mini/goods/category').then(d => this.cats = d || [])
    this.load()
  },
  methods: {
    img(p) { return p.coverUrl ? mediaUrl(p.coverUrl, BASE) : visual.gift },
    pick(id) {
      this.catId = id
      this.load()
    },
    load() {
      api.get('/api/mini/goods/product', { categoryId: this.catId || undefined })
        .then(d => this.list = d || [])
        .finally(() => this.loaded = true)
    },
    go(id) { uni.navigateTo({ url: `/pages/goods/detail?id=${id}` }) }
  }
}
</script>

<style scoped>
.intro { padding: 28rpx 36rpx 8rpx; }
.intro-title { font-size: 44rpx; font-weight: 500; margin-top: 8rpx; }
.intro-desc { color: #8A8175; font-size: 26rpx; line-height: 1.6; margin-top: 10rpx; }
.cats { white-space: nowrap; padding: 20rpx 28rpx 4rpx; }
.cat { display: inline-block; margin-right: 28rpx; padding-bottom: 10rpx; font-size: 26rpx; color: #8A8175; }
.cat.on { color: #1C1915; font-weight: 600; border-bottom: 4rpx solid #1C1915; }
.grid { display: flex; flex-wrap: wrap; justify-content: space-between; padding: 16rpx 28rpx 28rpx; }
.gitem { width: 336rpx; margin-bottom: 24rpx; background: #FFFcf8; border-radius: 24rpx; overflow: hidden; border: 1rpx solid rgba(28,25,21,.06); }
.frame { position: relative; }
.cover { width: 336rpx; height: 336rpx; display: block; }
.sold { position: absolute; left: 16rpx; top: 16rpx; background: rgba(28,25,21,.72); color: #F6E7C8; font-size: 20rpx; letter-spacing: 2rpx; border-radius: 999rpx; padding: 6rpx 16rpx; }
.gbody { padding: 18rpx 18rpx 22rpx; }
.name { font-size: 28rpx; font-weight: 500; line-height: 1.4; }
.line { margin-top: 6rpx; height: 68rpx; overflow: hidden; }
.price { color: #8C6A3D; font-size: 30rpx; font-weight: 600; margin-top: 10rpx; }
</style>
