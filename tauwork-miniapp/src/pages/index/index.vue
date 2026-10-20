<template>
  <view class="home">
    <view class="hero">
      <image class="hero-bg" src="/static/visual/harbor.jpg" mode="aspectFill" />
      <view class="hero-shade"></view>
      <view class="hero-inner" :style="{ paddingTop: (statusBar + 8) + 'px', paddingRight: capsulePad + 'px' }">
        <view class="brand">
          <image class="logo" src="/static/logo.png" mode="aspectFill" />
          <view>
            <view class="brand-en">GOODNESS</view>
            <view class="brand-cn">九千善</view>
          </view>
        </view>
        <view class="hero-copy">
          <view class="hero-kicker">HONG KONG  ·  SHENZHEN</view>
          <view class="hero-title">跨境生活</view>
          <view class="hero-title">安放在同一处</view>
          <view class="hero-desc lead">身份、公司与教育</view>
          <view class="hero-desc">以及可以坐下的社区</view>
        </view>
      </view>
    </view>

    <view class="panel">
      <view class="quick">
        <view class="qi" v-for="q in quicks" :key="q.label" @tap="go(q.url)">
          <image class="qimg" :src="q.img" mode="aspectFill" />
          <text class="ql">{{ q.label }}</text>
          <text class="qe">{{ q.en }}</text>
        </view>
      </view>

      <view class="block" v-if="banners.length">
        <text class="kicker">SELECTED</text>
        <text class="block-title">此刻推荐</text>
      </view>
      <swiper v-if="banners.length" class="banner" :circular="banners.length > 1" autoplay :interval="4800" @change="e => cur = e.detail.current">
        <swiper-item v-for="b in banners" :key="b.id">
          <view class="slide" @tap="onBanner(b)">
            <image class="slide-img" :src="b.image" mode="aspectFill" />
            <view class="slide-shade"></view>
            <view class="slide-copy">
              <text class="slide-badge" v-if="b.badge">{{ b.badge }}</text>
              <view class="slide-title">{{ b.title }}</view>
              <view class="slide-sub">{{ b.subtitle }}</view>
            </view>
            <text v-if="banners.length > 1" class="slide-idx">{{ pad(cur + 1) }} / {{ pad(banners.length) }}</text>
          </view>
        </swiper-item>
      </swiper>

      <view class="block" v-if="communities.length">
        <text class="kicker">COMMUNITIES</text>
        <view class="block-row">
          <text class="block-title">社区</text>
          <text class="block-meta">{{ communities.length }} 处</text>
        </view>
      </view>
      <view class="loc" v-for="c in communities" :key="c.id" @tap="goSpace(c.id)">
        <image class="loc-img" :src="c.image" mode="aspectFill" />
        <view class="loc-shade"></view>
        <view class="loc-copy">
          <view class="loc-name">{{ c.name }}</view>
          <view class="loc-addr">{{ c.address }}</view>
          <view class="loc-hours">{{ c.openHours }}</view>
        </view>
      </view>

      <view class="block" v-if="stories.length">
        <text class="kicker">STORIES</text>
        <view class="block-row">
          <text class="block-title">客户故事</text>
          <text class="block-more" @tap="go('/pages/story/list')">全部</text>
        </view>
      </view>
      <scroll-view v-if="stories.length" scroll-x class="stories" :show-scrollbar="false">
        <view class="story" v-for="s in stories" :key="s.id" @tap="go(`/pages/story/detail?id=${s.id}`)">
          <image class="story-img" :src="s.image" mode="aspectFill" />
          <view class="story-body">
            <view class="story-title">{{ s.title }}</view>
            <view class="story-foot">
              <text class="story-badge" v-if="s.badge">{{ s.badge }}</text>
              <text class="story-who">{{ s.subtitle }}</text>
            </view>
          </view>
        </view>
      </scroll-view>

      <view class="block" v-if="news.length">
        <text class="kicker">JOURNAL</text>
        <view class="block-row">
          <text class="block-title">公告资讯</text>
          <text class="block-more" @tap="go('/pages/news/news')">全部</text>
        </view>
      </view>
      <view class="news" v-if="news.length">
        <view class="nitem" v-for="n in news" :key="n.id" @tap="go(`/pages/news/detail?id=${n.id}`)">
          <image class="nimg" :src="n.image" mode="aspectFill" />
          <view class="nbody">
            <view class="ntitle">{{ n.title }}</view>
            <view class="sub">{{ (n.createTime || '').slice(0, 10) }}<text v-if="n.badge">  ·  {{ n.badge }}</text></view>
          </view>
        </view>
      </view>

      <view class="colophon">
        <text class="kicker">GOODNESS</text>
        <text class="colo-cn">九千善 · 跨境创业一站式服务</text>
      </view>
    </view>
  </view>
</template>

<script>
import api, { BASE, capturePartner } from '../../utils/request'
import { visual, communityImage, bannerImage, storyImage, newsImage, mediaUrl } from '../../utils/visual'

export default {
  data() {
    return {
      banners: [], stories: [], news: [], communities: [],
      cur: 0,
      statusBar: 20,
      capsulePad: 24,
      quicks: [
        { img: visual.identity, label: '评估', en: 'ASSESS', url: '/pages/assess/assess' },
        { img: visual.desk, label: '社区', en: 'PLACE', url: '/pages/space/space' },
        { img: visual.company, label: '服务', en: 'SERVICE', url: '/pages/svc/svc' },
        { img: visual.gift, label: '礼品', en: 'GIFT', url: '/pages/goods/list' },
        { img: visual.journal, label: '预订', en: 'ORDERS', url: '/pages/me/bookings' }
      ]
    }
  },
  created() {
    const sys = uni.getSystemInfoSync()
    this.statusBar = sys.statusBarHeight || 20
    try {
      const m = uni.getMenuButtonBoundingClientRect()
      if (m && m.left) this.capsulePad = Math.max(24, sys.windowWidth - m.left + 12)
    } catch (e) {}
  },
  onLoad(options) {
    capturePartner({ query: options || {} })
  },
  onShareAppMessage() {
    const code = uni.getStorageSync('myPartnerCode')
    return {
      title: '九千善',
      path: code ? `/pages/index/index?partner=${code}` : '/pages/index/index'
    }
  },
  onShow() {
    api.get('/api/mini/cms/banner').then(d => {
      this.banners = (d || []).map(b => ({
        ...b,
        image: b.coverUrl ? mediaUrl(b.coverUrl, BASE) : bannerImage(b)
      }))
    })
    api.get('/api/mini/cms/story').then(d => {
      this.stories = (d || []).map((s, i) => ({
        ...s,
        image: s.coverUrl ? mediaUrl(s.coverUrl, BASE) : storyImage(i)
      }))
    })
    api.get('/api/mini/cms/news', { page: 1, size: 3 }).then(d => {
      this.news = (d || []).map((n, i) => ({
        ...n,
        image: n.coverUrl ? mediaUrl(n.coverUrl, BASE) : newsImage(i)
      }))
    })
    api.get('/api/mini/space/community').then(d => {
      this.communities = (d || []).map(c => ({ ...c, image: communityImage(c.name) }))
    })
  },
  methods: {
    pad(n) { return String(n).padStart(2, '0') },
    go(url) {
      const path = url.split('?')[0]
      const tabs = ['/pages/space/space', '/pages/svc/svc', '/pages/me/me', '/pages/index/index']
      if (tabs.includes(path)) uni.switchTab({ url: path })
      else uni.navigateTo({ url })
    },
    goSpace(id) {
      uni.navigateTo({ url: `/pages/space/store?cid=${id}` })
    },
    onBanner(b) {
      const id = b.linkTarget
      if (b.linkType === 'ASSESS') uni.navigateTo({ url: '/pages/assess/assess' })
      else if (b.linkType === 'SVC' && id) uni.navigateTo({ url: `/pages/svc/detail?id=${id}` })
      else if (b.linkType === 'GOODS' && id) uni.navigateTo({ url: `/pages/goods/detail?id=${id}` })
      else if (b.linkType === 'NEWS' && id) uni.navigateTo({ url: `/pages/news/detail?id=${id}` })
      else if (b.linkType === 'STORY' && id) uni.navigateTo({ url: `/pages/story/detail?id=${id}` })
      else if (b.linkType === 'SPACE') {
        if (id) uni.navigateTo({ url: `/pages/space/store?cid=${id}` })
        else uni.switchTab({ url: '/pages/space/space' })
      }
    }
  }
}
</script>

<style scoped>
.hero { position: relative; height: 820rpx; }
.hero-bg, .hero-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.hero-shade { background: linear-gradient(180deg, rgba(14, 18, 24, 0.28) 0%, rgba(14, 18, 24, 0.05) 38%, rgba(14, 18, 24, 0.78) 100%); }
.hero-inner {
  position: relative; z-index: 2; height: 100%; box-sizing: border-box;
  display: flex; flex-direction: column; justify-content: space-between;
  padding-left: 40rpx; padding-bottom: 88rpx;
}
.brand { display: flex; align-items: center; gap: 18rpx; }
.logo { width: 72rpx; height: 72rpx; border-radius: 18rpx; }
.brand-en { color: rgba(255,255,255,.72); font-size: 18rpx; letter-spacing: 6rpx; }
.brand-cn { color: #fff; font-size: 32rpx; margin-top: 2rpx; }
.hero-kicker { color: #E7D3AE; font-size: 20rpx; letter-spacing: 5rpx; margin-bottom: 18rpx; }
.hero-title { color: #F7F3EC; font-size: 64rpx; font-weight: 500; line-height: 1.18; letter-spacing: 2rpx; }
.hero-desc { color: rgba(247,243,236,.82); font-size: 26rpx; line-height: 1.55; margin-top: 4rpx; }
.lead { margin-top: 20rpx; }
.panel { margin-top: -40rpx; position: relative; background: #F3EEE6; border-radius: 36rpx 36rpx 0 0; padding-top: 28rpx; }
.quick { display: flex; padding: 16rpx 8rpx 8rpx; }
.qi { flex: 1; display: flex; flex-direction: column; align-items: center; }
.qimg { width: 96rpx; height: 96rpx; border-radius: 28rpx; }
.ql { margin-top: 12rpx; font-size: 22rpx; color: #1C1915; }
.qe { margin-top: 2rpx; font-size: 16rpx; letter-spacing: 1rpx; color: #A3988C; }
.block { padding: 36rpx 36rpx 18rpx; }
.block-title { display: block; font-size: 40rpx; font-weight: 500; margin-top: 8rpx; letter-spacing: 1rpx; }
.block-row { display: flex; align-items: baseline; justify-content: space-between; }
.block-meta, .block-more { color: #8A8175; font-size: 24rpx; }
.banner { height: 380rpx; }
.slide { position: relative; height: 380rpx; margin: 0 28rpx; border-radius: 28rpx; overflow: hidden; }
.slide-img, .slide-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.slide-shade { background: linear-gradient(180deg, rgba(16,14,12,0) 30%, rgba(16,14,12,.72) 100%); }
.slide-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 28rpx 32rpx 32rpx; color: #fff; }
.slide-badge { font-size: 20rpx; letter-spacing: 2rpx; color: #1C1915; background: #E7D3AE; border-radius: 999rpx; padding: 4rpx 16rpx; }
.slide-title { font-size: 36rpx; font-weight: 500; margin-top: 12rpx; }
.slide-sub { font-size: 24rpx; color: rgba(255,255,255,.8); margin-top: 8rpx; line-height: 1.5; }
.slide-idx { position: absolute; right: 24rpx; top: 24rpx; color: #fff; font-size: 20rpx; letter-spacing: 2rpx; background: rgba(20,16,12,.35); border-radius: 999rpx; padding: 6rpx 16rpx; }
.loc { position: relative; height: 320rpx; margin: 0 28rpx 20rpx; border-radius: 28rpx; overflow: hidden; }
.loc-img, .loc-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.loc-shade { background: linear-gradient(180deg, rgba(16,14,12,0) 28%, rgba(16,14,12,.7) 100%); }
.loc-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 28rpx 32rpx; color: #fff; }
.loc-name { font-size: 34rpx; font-weight: 500; }
.loc-addr, .loc-hours { font-size: 22rpx; color: rgba(255,255,255,.82); margin-top: 6rpx; }
.stories { white-space: nowrap; height: 520rpx; padding-left: 28rpx; }
.story {
  display: inline-block; width: 460rpx; height: 500rpx; margin-right: 20rpx;
  background: #FFFcf8; border-radius: 28rpx; overflow: hidden; vertical-align: top; white-space: normal;
  border: 1rpx solid rgba(28,25,21,.06);
}
.story-img { width: 460rpx; height: 240rpx; display: block; }
.story-body { padding: 24rpx 24rpx 0; }
.story-title {
  font-size: 28rpx; line-height: 1.6; height: 134rpx; overflow: hidden;
}
.story-foot { display: flex; align-items: center; gap: 12rpx; margin-top: 8rpx; }
.story-badge { font-size: 20rpx; color: #6F6252; background: #F3EEE6; border-radius: 999rpx; padding: 4rpx 14rpx; }
.story-who { font-size: 22rpx; color: #8A8175; }
.news { margin: 0 28rpx; background: #FFFcf8; border-radius: 28rpx; overflow: hidden; border: 1rpx solid rgba(28,25,21,.06); }
.nitem { display: flex; gap: 20rpx; padding: 22rpx; }
.nitem + .nitem { border-top: 1rpx solid #F0EBE3; }
.nimg { width: 148rpx; height: 112rpx; border-radius: 16rpx; flex-shrink: 0; }
.nbody { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.ntitle { font-size: 28rpx; line-height: 1.45; margin-bottom: 8rpx; }
.colophon { padding: 48rpx 36rpx 36rpx; }
.colo-cn { display: block; margin-top: 10rpx; color: #8A8175; font-size: 24rpx; }
</style>
