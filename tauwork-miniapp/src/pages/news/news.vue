<template>
  <view class="page">
    <view class="intro">
      <text class="kicker">JOURNAL</text>
      <view class="intro-title">公告资讯</view>
    </view>
    <view class="item" v-for="(n, i) in list" :key="n.id" @tap="go(n.id)">
      <image class="cover" :src="n.image" mode="aspectFill" />
      <view class="body">
        <text class="kicker" v-if="n.badge">{{ n.badge }}</text>
        <view class="title">{{ n.title }}</view>
        <view class="sub">{{ (n.effectiveAt || n.createTime || '').slice(0, 10) }}</view>
      </view>
    </view>
  </view>
</template>

<script>
import api, { BASE } from '../../utils/request'
import { mediaUrl, newsImage } from '../../utils/visual'

export default {
  data() {
    return { list: [] }
  },
  onShow() {
    api.get('/api/mini/cms/news', { page: 1, size: 20 }).then(d => {
      this.list = (d || []).map((n, i) => ({
        ...n,
        image: n.coverUrl ? mediaUrl(n.coverUrl, BASE) : newsImage(i)
      }))
    })
  },
  methods: {
    go(id) { uni.navigateTo({ url: `/pages/news/detail?id=${id}` }) }
  }
}
</script>

<style scoped>
.intro { padding: 28rpx 36rpx 12rpx; }
.intro-title { font-size: 44rpx; font-weight: 500; margin-top: 8rpx; }
.item { margin: 0 28rpx 20rpx; background: #FFFcf8; border-radius: 24rpx; overflow: hidden; border: 1rpx solid rgba(28,25,21,.06); }
.cover { width: 100%; height: 280rpx; display: block; }
.body { padding: 22rpx 28rpx 26rpx; }
.title { font-size: 32rpx; font-weight: 500; line-height: 1.45; margin: 8rpx 0; }
</style>
