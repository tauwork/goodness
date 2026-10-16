<template>
  <view class="page">
    <!-- 社区店切换 -->
    <scroll-view scroll-x class="tabbar">
      <view v-for="c in communities" :key="c.id" class="tab" :class="{ on: c.id === currentId }"
            @tap="switchTo(c.id)">{{ c.name }}</view>
    </scroll-view>

    <view v-if="current" class="card">
      <view class="cinfo">
        <view class="cname">{{ current.name }}</view>
        <view class="sub">{{ current.address }}</view>
        <view class="sub">{{ current.openHours }}</view>
        <view class="fac">
          <text class="tag" v-for="f in facilities" :key="f">{{ f }}</text>
        </view>
      </view>
    </view>

    <!-- 场地列表 -->
    <view class="card sp" v-for="p in products" :key="p.communityProductId"
          @tap="go(`/pages/space/detail?cpid=${p.communityProductId}`)">
      <view class="spbody">
        <view class="spname">{{ p.name }} <text class="tag">{{ p.categoryName }}</text></view>
        <view class="sub">{{ p.subtitle }}</view>
        <view class="spfoot">
          <text class="price">¥{{ p.price }}<text class="unit">/{{ unitText(p.priceUnit) }}</text></text>
          <text class="stock" v-if="p.stock !== null">余 {{ p.stock }}</text>
          <text class="book">预订 ›</text>
        </view>
      </view>
    </view>
    <view v-if="!products.length" class="card" style="text-align:center;color:#8A94A8">该店暂无可售场地</view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { communities: [], currentId: null, current: null, products: [] }
  },
  computed: {
    facilities() {
      try { return JSON.parse(this.current.facilities || '[]') } catch (e) { return [] }
    }
  },
  onLoad(q) {
    api.get('/api/mini/space/community').then(list => {
      this.communities = list
      this.switchTo(q.cid ? +q.cid : (list[0] && list[0].id))
    })
  },
  methods: {
    switchTo(id) {
      this.currentId = id
      this.current = this.communities.find(c => c.id === id)
      api.get(`/api/mini/space/community/${id}/product`).then(d => this.products = d)
    },
    unitText(u) {
      return { HOUR: '小时', DAY: '天', MONTH: '月', ONCE: '场' }[u] || '次'
    },
    go(url) { uni.navigateTo({ url }) }
  }
}
</script>

<style scoped>
.tabbar { white-space: nowrap; padding: 24rpx 24rpx 8rpx; }
.tab { display: inline-block; background: #fff; border-radius: 22rpx; padding: 14rpx 30rpx;
  font-size: 26rpx; color: #8A94A8; margin-right: 16rpx; }
.tab.on { background: #16355E; color: #fff; font-weight: 600; }
.cname { font-size: 32rpx; font-weight: 600; margin-bottom: 10rpx; }
.fac { margin-top: 14rpx; }
.sp { padding: 30rpx; }
.spname { font-size: 30rpx; font-weight: 600; margin-bottom: 8rpx; }
.spfoot { display: flex; align-items: center; margin-top: 18rpx; }
.price { color: #A8853F; font-size: 32rpx; font-weight: 700; }
.unit { font-size: 20rpx; font-weight: 400; }
.stock { font-size: 22rpx; color: #2FA36B; margin-left: 20rpx; }
.book { margin-left: auto; color: #16355E; background: #EDF2F9; border-radius: 24rpx; padding: 8rpx 24rpx; font-size: 24rpx; }
</style>
