<template>
  <view class="page">
    <!-- 品牌头 -->
    <view class="head">
      <view class="brand">
        <image class="logo-img" src="/static/logo.png" mode="aspectFit" />
        <view>
          <view class="bname">九千善 <text class="badge">跨境创业一站式服务平台</text></view>
          <view class="bdesc">身份规划 · 国际教育 · 跨境创业</view>
        </view>
      </view>
    </view>

    <!-- 广告轮播（后台 CMS 配置） -->
    <swiper class="banner" indicator-dots indicator-color="rgba(255,255,255,.4)"
            indicator-active-color="#C2A05F" autoplay circular :interval="4000" @change="e => cur = e.detail.current">
      <swiper-item v-for="b in banners" :key="b.id" @tap="onBanner(b)">
        <view class="slide" :style="{ background: styleBg(b.coverStyle) }">
          <view class="ht">{{ b.title }}<text class="hb">{{ b.badge }}</text></view>
          <view class="hd">{{ b.subtitle }}</view>
          <text class="hbtn">立即了解</text>
        </view>
      </swiper-item>
    </swiper>

    <!-- 快捷服务 -->
    <view class="quick">
      <view class="qi" v-for="q in quicks" :key="q.label" @tap="go(q.url)">
        <text class="qic" :style="{ background: q.bg, color: q.color }">{{ q.icon }}</text>
        <text>{{ q.label }}</text>
      </view>
    </view>

    <!-- 全球网点 -->
    <view class="sect"><h3>全球服务网点</h3><text class="sub">3 家社区店</text></view>
    <view class="card" v-for="c in communities" :key="c.id" @tap="go(`/pages/space/space?cid=${c.id}`)">
      <view class="crow">
        <view class="cinfo">
          <view class="cname">{{ c.name }}</view>
          <view class="sub">{{ c.address }}</view>
          <view class="sub">{{ c.openHours }}</view>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 客户故事（CMS） -->
    <view class="sect"><h3>客户故事</h3></view>
    <scroll-view scroll-x class="stories">
      <view class="story" v-for="s in stories" :key="s.id">
        <text class="sq">“</text>
        <view class="st">{{ s.title }}</view>
        <view class="sfoot">
          <text class="sres">{{ s.badge }}</text>
          <text class="sub">{{ s.subtitle }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 公告资讯 -->
    <view class="sect"><h3>公告资讯</h3><text class="sub" @tap="go('/pages/news/news')">更多 ›</text></view>
    <view class="card" style="padding: 10rpx 28rpx;">
      <view class="nitem" v-for="n in news" :key="n.id" @tap="go(`/pages/news/detail?id=${n.id}`)">
        <text class="ndot"></text>
        <view style="flex:1">
          <view class="ntitle">{{ n.title }}</view>
          <view class="sub">{{ (n.createTime || '').slice(0, 10) }} · {{ n.badge }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return {
      banners: [], stories: [], news: [], communities: [],
      cur: 0,
      quicks: [
        { icon: '评', label: '智能评估', url: '/pages/svc/svc', bg: '#E8F5EE', color: '#2FA36B' },
        { icon: '订', label: '订空间', url: '/pages/space/space', bg: '#F7F1E2', color: '#A8853F' },
        { icon: '服', label: '全部服务', url: '/pages/svc/svc', bg: '#EDF2F9', color: '#16355E' },
        { icon: '码', label: '我的预订', url: '/pages/me/bookings', bg: '#F0EDF8', color: '#5B4C9E' }
      ]
    }
  },
  onShow() {
    api.get('/api/mini/cms/banner').then(d => this.banners = d)
    api.get('/api/mini/cms/story').then(d => this.stories = d)
    api.get('/api/mini/cms/news', { page: 1, size: 3 }).then(d => this.news = d)
    api.get('/api/mini/space/community').then(d => this.communities = d)
  },
  methods: {
    go(url) { uni.navigateTo({ url: url.replace('navigateTo', '') }) },
    styleBg(style) {
      const map = {
        NAVY: 'linear-gradient(135deg,#16355E,#2E5C9E)',
        GOLD: 'linear-gradient(135deg,#A8853F,#C2A05F)',
        PURPLE: 'linear-gradient(135deg,#5B4C9E,#8474C4)',
        TEAL: 'linear-gradient(135deg,#0B6B6B,#2FA36B)'
      }
      return map[style] || map.NAVY
    },
    onBanner(b) {
      if (b.linkType === 'SVC') uni.navigateTo({ url: '/pages/svc/svc' })
      else if (b.linkType === 'ASSESS') uni.navigateTo({ url: '/pages/svc/svc' })
    }
  }
}
</script>

<style scoped>
.head { background: linear-gradient(160deg,#16355E,#0E2544); padding: 20rpx 32rpx 60rpx; }
.brand { display: flex; align-items: center; gap: 20rpx; }
.logo-img { width: 76rpx; height: 76rpx; border-radius: 22rpx; }
.bname { color: #fff; font-size: 34rpx; font-weight: 600; }
.badge { font-size: 18rpx; color: #C2A05F; border: 1rpx solid #C2A05F; border-radius: 8rpx; padding: 2rpx 10rpx; margin-left: 12rpx; vertical-align: 4rpx; font-weight: 400; }
.bdesc { color: #9DB2D2; font-size: 20rpx; margin-top: 6rpx; }
.banner { height: 220rpx; margin: -40rpx 24rpx 24rpx; border-radius: 28rpx; overflow: hidden; }
.slide { height: 220rpx; border-radius: 28rpx; padding: 32rpx; color: #fff; }
.ht { font-size: 34rpx; font-weight: 600; }
.hb { font-size: 18rpx; color: #0E2544; background: #C2A05F; border-radius: 20rpx; padding: 4rpx 16rpx; margin-left: 16rpx; vertical-align: 6rpx; font-weight: 600; }
.hd { font-size: 22rpx; color: rgba(255,255,255,.75); margin: 12rpx 0 20rpx; }
.hbtn { background: #C2A05F; color: #0E2544; font-size: 24rpx; font-weight: 600; border-radius: 30rpx; padding: 12rpx 32rpx; }
.quick { display: flex; background: #fff; border-radius: 24rpx; margin: 0 24rpx 24rpx; padding: 24rpx 0; }
.qi { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 14rpx; font-size: 22rpx; }
.qic { width: 88rpx; height: 88rpx; border-radius: 28rpx; display: flex; align-items: center; justify-content: center; font-size: 36rpx; }
.crow { display: flex; align-items: center; }
.cinfo { flex: 1; }
.cname { font-size: 30rpx; font-weight: 600; margin-bottom: 8rpx; }
.arrow { color: #C3CBD8; font-size: 40rpx; }
.stories { white-space: nowrap; padding: 0 24rpx 24rpx; }
.story { display: inline-block; width: 500rpx; background: #fff; border-radius: 24rpx; padding: 28rpx; margin-right: 20rpx; vertical-align: top; white-space: normal; }
.sq { color: #C2A05F; font-size: 52rpx; line-height: 12rpx; font-family: Georgia; }
.st { font-size: 24rpx; line-height: 1.7; margin: 16rpx 0 20rpx; min-height: 120rpx; }
.sfoot { display: flex; align-items: center; gap: 14rpx; border-top: 1rpx dashed #E8ECF3; padding-top: 18rpx; }
.sres { font-size: 20rpx; color: #2FA36B; background: #E8F5EE; border-radius: 8rpx; padding: 4rpx 14rpx; }
.nitem { display: flex; align-items: center; gap: 16rpx; padding: 24rpx 0; border-bottom: 1rpx solid #F1F4F8; }
.nitem:last-child { border: none; }
.ndot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #C2A05F; }
.ntitle { font-size: 27rpx; }
</style>
