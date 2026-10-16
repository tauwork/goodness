<template>
  <view class="page">
    <!-- 服务分类（后台可配置） -->
    <view class="cats">
      <view class="cat" :class="{ on: !catId }" @tap="pick(null)">全部</view>
      <view class="cat" v-for="c in cats" :key="c.id" :class="{ on: catId === c.id }"
            @tap="pick(c.id)">{{ c.name }}</view>
    </view>

    <view class="card" v-for="p in list" :key="p.id" @tap="go(p.id)">
      <view class="name">{{ p.name }}</view>
      <view class="sub" style="margin:8rpx 0 16rpx">{{ p.subtitle }}</view>
      <view class="foot">
        <text class="price">¥{{ p.priceValue }}<text v-if="p.priceMode==='STAGED'"> 起</text></text>
        <text class="ask">查看详情 ›</text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { cats: [], list: [], catId: null }
  },
  onShow() {
    api.get('/api/mini/svc/category').then(d => this.cats = d)
    this.load()
  },
  methods: {
    pick(id) {
      this.catId = id
      this.load()
    },
    load() {
      api.get('/api/mini/svc/product', { categoryId: this.catId || undefined }).then(d => this.list = d)
    },
    go(id) { uni.navigateTo({ url: `/pages/svc/detail?id=${id}` }) }
  }
}
</script>

<style scoped>
.cats { display: flex; flex-wrap: wrap; gap: 14rpx; padding: 24rpx; }
.cat { background: #fff; border-radius: 22rpx; padding: 12rpx 28rpx; font-size: 24rpx; color: #8A94A8; }
.cat.on { background: #16355E; color: #fff; font-weight: 600; }
.name { font-size: 30rpx; font-weight: 600; }
.foot { display: flex; align-items: center; margin-top: 6rpx; }
.price { color: #A8853F; font-size: 30rpx; font-weight: 700; }
.quote { color: #8A94A8; font-size: 26rpx; }
.ask { margin-left: auto; color: #16355E; background: #EDF2F9; border-radius: 24rpx; padding: 8rpx 24rpx; font-size: 24rpx; }
</style>
