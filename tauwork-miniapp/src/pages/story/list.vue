<template>
  <view class="page">
    <view class="intro">
      <text class="kicker">STORIES</text>
      <view class="intro-title">客户故事</view>
    </view>
    <view class="item" v-for="s in list" :key="s.id" @tap="go(s.id)">
      <image class="cover" :src="s.image" mode="aspectFill" />
      <view class="body">
        <text class="kicker" v-if="s.badge">{{ s.badge }}</text>
        <view class="title">{{ s.title }}</view>
        <view class="sub" v-if="s.subtitle">{{ s.subtitle }}</view>
      </view>
    </view>
    <view class="end" v-if="list.length">已显示全部故事</view>
    <view class="empty-box" v-else>
      <image src="/static/visual/journal.jpg" mode="aspectFill" />
      <text>还没有客户故事</text>
    </view>
  </view>
</template>

<script>
import api, { BASE } from '../../utils/request'
import { mediaUrl, storyImage } from '../../utils/visual'

export default {
  data() {
    return { list: [] }
  },
  onShow() {
    api.get('/api/mini/cms/story').then(d => {
      this.list = (d || []).map((s, i) => ({
        ...s,
        image: s.coverUrl ? mediaUrl(s.coverUrl, BASE) : storyImage(i)
      }))
    })
  },
  onShareAppMessage() {
    return { title: '九千善 · 客户故事', path: '/pages/story/list' }
  },
  onShareTimeline() {
    return { title: '九千善 · 客户故事' }
  },
  methods: {
    go(id) { uni.navigateTo({ url: `/pages/story/detail?id=${id}` }) }
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
.end { text-align: center; color: #A3988C; font-size: 24rpx; padding: 12rpx 0 48rpx; }
</style>
