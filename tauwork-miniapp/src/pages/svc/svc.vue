<template>
  <view class="page">
    <view class="intro">
      <view class="intro-title">您的贴心管家</view>
      <view class="intro-desc">有温度的专业化私人定制服务</view>
    </view>

    <view class="sheet">
      <view class="tabs">
        <view class="tab" :class="{ on: !catId }" @tap="pick(null)">
          <image class="tab-icon" src="/static/visual/harbor.jpg" mode="aspectFill" />
          <text class="tab-name">全部</text>
          <view class="tab-bar"></view>
        </view>
        <view class="tab" v-for="c in cats" :key="c.id" :class="{ on: same(catId, c.id) }" @tap="pick(c.id)">
          <image class="tab-icon" :src="c.image" mode="aspectFill" />
          <text class="tab-name">{{ c.name }}</text>
          <view class="tab-bar"></view>
        </view>
      </view>

      <view class="panel">
        <view class="item" v-for="p in list" :key="p.id" @tap="go(p.id)">
          <image class="cover" :src="p.image" mode="aspectFill" />
          <view class="info">
            <view class="name">{{ p.name }}</view>
            <view class="sub">{{ p.subtitle }}</view>
            <view class="price">¥{{ p.priceValue }}<text v-if="p.priceMode==='STAGED'" class="from"> 起</text></view>
          </view>
        </view>
        <view v-if="loaded && !list.length" class="empty">这一类暂无在售服务</view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'
import { serviceImage } from '../../utils/visual'

export default {
  data() {
    return { cats: [], list: [], catId: null, loaded: false }
  },
  onShow() {
    api.get('/api/mini/svc/category').then(d => {
      this.cats = (d || []).map(c => ({ ...c, image: serviceImage(c) }))
    })
    this.load()
  },
  methods: {
    same(a, b) {
      return a != null && String(a) === String(b)
    },
    pick(id) {
      const cat = this.cats.find(c => this.same(c.id, id))
      const name = (cat && cat.name) || ''
      const code = (cat && cat.code) || ''
      if (code === 'COMPANY' || name.includes('公司注册')) {
        uni.navigateTo({ url: `/pages/svc/detail?categoryId=${id}` })
        return
      }
      this.catId = id
      this.load()
    },
    load() {
      this.loaded = false
      api.get('/api/mini/svc/product', { categoryId: this.catId || undefined }).then(d => {
        this.list = (d || []).map(p => ({ ...p, image: serviceImage(p) }))
      }).finally(() => { this.loaded = true })
    },
    go(id) { uni.navigateTo({ url: `/pages/svc/detail?id=${id}` }) }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F2F2F7; padding-bottom: 48rpx; }
.intro { padding: 28rpx 32rpx 8rpx; }
.intro-title { color: #1D1D1F; font-size: 40rpx; font-weight: 600; letter-spacing: -0.5rpx; }
.intro-desc { color: #86868B; font-size: 26rpx; line-height: 1.6; margin-top: 10rpx; }
.sheet {
  margin: 20rpx 16rpx 0; background: #fff; border-radius: 28rpx; overflow: hidden;
  box-shadow: 0 12rpx 32rpx rgba(0,0,0,.04);
}
.tabs { display: flex; align-items: flex-start; padding: 24rpx 0 0; }
.tab {
  flex: 1; min-width: 0; display: flex; flex-direction: column; align-items: center;
  padding-bottom: 16rpx;
}
.tab-icon { width: 56rpx; height: 56rpx; border-radius: 16rpx; background: #F2F2F7; }
.tab-name {
  width: 100%; margin-top: 8rpx; box-sizing: border-box;
  text-align: center; font-size: 20rpx; line-height: 1.25; color: #86868B;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.tab.on .tab-name { color: #1D1D1F; font-weight: 600; }
.tab-bar { width: 36rpx; height: 4rpx; margin-top: 10rpx; border-radius: 4rpx; background: transparent; }
.tab.on .tab-bar { background: #1D1D1F; }
.panel { border-top: 1rpx solid #F2F2F7; }
.item { display: flex; gap: 20rpx; padding: 22rpx 24rpx; }
.item + .item { border-top: 1rpx solid #F2F2F7; }
.cover { width: 140rpx; height: 140rpx; border-radius: 16rpx; flex-shrink: 0; background: #E5E5EA; }
.info { flex: 1; min-width: 0; }
.name { font-size: 28rpx; font-weight: 500; color: #1D1D1F; line-height: 1.35; }
.sub {
  margin-top: 6rpx; color: #86868B; font-size: 22rpx; line-height: 1.4;
  overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.price { color: #1D1D1F; font-size: 28rpx; font-weight: 600; margin-top: 10rpx; }
.from { font-size: 22rpx; font-weight: 400; color: #86868B; }
.empty { text-align: center; color: #86868B; font-size: 24rpx; padding: 48rpx 24rpx; }
</style>
