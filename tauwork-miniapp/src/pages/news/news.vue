<template>
  <view class="page">
    <view class="card" v-for="n in list" :key="n.id" @tap="go(n.id)">
      <view class="ntitle">{{ n.title }}</view>
      <view class="sub" style="margin-top:10rpx">{{ n.badge }} · {{ (n.effectiveAt || n.createTime || '').slice(0, 10) }}</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { list: [] }
  },
  onShow() {
    api.get('/api/mini/cms/news', { page: 1, size: 20 }).then(d => this.list = d)
  },
  methods: {
    go(id) { uni.navigateTo({ url: `/pages/news/detail?id=${id}` }) }
  }
}
</script>

<style scoped>
.ntitle { font-size: 30rpx; font-weight: 500; }
</style>
