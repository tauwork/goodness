<template>
  <view class="page sink" v-if="n">
    <image class="cover" :src="cover" mode="aspectFill" />
    <view class="card sheet">
      <text class="kicker">{{ n.badge || 'JOURNAL' }}</text>
      <view class="title">{{ n.title }}</view>
      <view class="sub date">{{ (n.effectiveAt || n.createTime || '').replace('T', ' ').slice(0, 10) }}</view>
      <view class="sub lead" v-if="n.subtitle && n.body">{{ n.subtitle }}</view>
      <view class="body">{{ n.body || n.subtitle }}</view>
    </view>
    <view class="paybar">
      <button class="sharebtn" open-type="share">分享转发</button>
    </view>
  </view>
</template>

<script>
import api, { BASE } from '../../utils/request'
import { mediaUrl, visual } from '../../utils/visual'

export default {
  data() {
    return { n: null, cover: visual.journal }
  },
  onLoad(q) {
    this.id = q.id
    uni.showShareMenu({ menus: ['shareAppMessage', 'shareTimeline'] })
    api.get(`/api/mini/cms/news/${q.id}`).then(d => {
      this.n = d
      this.cover = d.coverUrl ? mediaUrl(d.coverUrl, BASE) : visual.journal
    })
  },
  onShareAppMessage() {
    const n = this.n || {}
    return {
      title: n.title || '九千善 · 公告资讯',
      path: `/pages/news/detail?id=${n.id || this.id || ''}`,
      imageUrl: this.shareImage()
    }
  },
  onShareTimeline() {
    const n = this.n || {}
    return {
      title: n.title || '九千善 · 公告资讯',
      query: `id=${n.id || this.id || ''}`,
      imageUrl: this.shareImage()
    }
  },
  methods: {
    shareImage() {
      const u = this.cover || ''
      if (u.startsWith('https://') || u.startsWith('/static/')) return u
      return visual.journal
    }
  }
}
</script>

<style scoped>
.cover { width: 100%; height: 420rpx; display: block; }
.sheet { margin-top: -36rpx; position: relative; }
.title { font-size: 40rpx; font-weight: 500; line-height: 1.45; margin-top: 12rpx; }
.date { margin: 16rpx 0 8rpx; }
.lead { margin-bottom: 24rpx; }
.body { font-size: 30rpx; line-height: 1.9; color: #3C362F; white-space: pre-wrap; }
.sharebtn {
  flex: 1;
  background: #1C1915;
  color: #F6E7C8;
  border-radius: 999rpx;
  font-size: 28rpx;
  letter-spacing: 2rpx;
  margin: 0;
  padding: 8rpx 0;
  line-height: 2.4;
}
.sharebtn::after { border: none; }
</style>
