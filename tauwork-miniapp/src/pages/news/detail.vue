<template>
  <view class="page" v-if="n">
    <view class="card">
      <view class="title">{{ n.title }}</view>
      <view class="sub" style="margin:12rpx 0 20rpx">{{ n.badge }} · {{ (n.effectiveAt || '').slice(0, 10) }}</view>
      <view class="body">{{ n.body || n.subtitle }}</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { n: null }
  },
  onLoad(q) {
    api.get(`/api/mini/cms/news/${q.id}`).then(d => this.n = d)
  }
}
</script>

<style scoped>
.title { font-size: 36rpx; font-weight: 600; line-height: 1.5; }
.body { font-size: 28rpx; line-height: 1.9; color: #3A4658; }
</style>
