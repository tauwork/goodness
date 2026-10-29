<template>
  <view class="home">
    <view class="nav" :style="{ paddingTop: statusBar + 'px', paddingRight: capsulePad + 'px' }">
      <view class="brand-row">
        <image class="brand-logo" :src="logo" mode="aspectFit" />
        <view>
          <view class="brand">九千善</view>
          <view class="brand-sub">跨境创业与生活服务平台</view>
        </view>
      </view>
    </view>

    <swiper v-if="banners.length" class="banner" :circular="banners.length > 1" autoplay :interval="5000"
            :indicator-dots="banners.length > 1" indicator-color="rgba(255,255,255,.45)" indicator-active-color="#ffffff">
      <swiper-item v-for="b in banners" :key="b.id">
        <view class="slide" @tap="onBanner(b)">
          <image class="slide-img" :src="b.image" mode="aspectFill" />
          <view class="slide-shade"></view>
          <view class="slide-copy">
            <view class="slide-title">{{ b.title }}</view>
            <view class="slide-sub" v-if="b.subtitle">{{ b.subtitle }}</view>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <view class="sheet">
      <view class="section-title">快捷服务</view>
      <view class="quick">
        <view class="qi" v-for="q in quicks" :key="q.key" @tap="openQuick(q)">
          <view class="qicon">
            <image class="qimg" :src="q.icon" mode="aspectFit" />
          </view>
          <text class="ql">{{ q.label }}</text>
        </view>
        <view class="qi contact">
          <view class="qicon">
            <image class="qimg" src="/static/icons/home-consult.png" mode="aspectFit" />
          </view>
          <text class="ql">1V1咨询</text>
          <cell
            class="contact-hit"
            plugid="790fd0f846a7e2ac117c8c1bee554bb1"
            styleType="2"
            blockStyle="button"
            buttonStyle="light"
            @completemessage="onContactDone"
          />
        </view>
      </view>
    </view>

    <view class="sheet" v-if="hotCommunities.length">
      <view class="section-head">
        <text class="section-title">社区</text>
        <text class="more" @tap="go('/pages/space/space')">全部</text>
      </view>
      <view class="places">
        <view class="place" v-for="c in hotCommunities" :key="c.id" @tap="goSpace(c.id)">
          <image class="place-img" :src="c.image" mode="aspectFill" />
          <view class="place-name">{{ c.name }}</view>
          <view class="place-addr">{{ c.district || c.city || '' }}</view>
        </view>
      </view>
    </view>

    <view class="sheet" v-if="stories.length">
      <view class="section-head">
        <text class="section-title">客户故事</text>
        <text class="more" @tap="go('/pages/story/list')">全部</text>
      </view>
      <view class="stories-clip">
        <scroll-view scroll-x class="stories" :show-scrollbar="false">
          <view class="stories-row">
            <view class="story" v-for="s in stories" :key="s.id" @tap="go(`/pages/story/detail?id=${s.id}`)">
              <image class="story-img" :src="s.image" mode="aspectFill" />
              <view class="story-title">{{ s.title }}</view>
              <view class="story-who" v-if="s.subtitle">{{ s.subtitle }}</view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <view class="sheet" v-if="news.length">
      <view class="section-head">
        <text class="section-title">最新资讯</text>
        <text class="more" @tap="go('/pages/news/news')">全部</text>
      </view>
      <view class="news">
        <view class="nitem" v-for="n in news" :key="n.id" @tap="go(`/pages/news/detail?id=${n.id}`)">
          <view class="nbody">
            <view class="ntitle">{{ n.title }}</view>
            <view class="ndate">{{ (n.createTime || '').slice(0, 10) }}</view>
          </view>
          <image v-if="n.image" class="nimg" :src="n.image" mode="aspectFill" />
        </view>
      </view>
    </view>

  </view>
</template>

<script>
import api, { BASE, capturePartner } from '../../utils/request'
import { communityImage, bannerImage, storyImage, newsImage, mediaUrl } from '../../utils/visual'

export default {
  data() {
    return {
      banners: [], stories: [], news: [], communities: [],
      statusBar: 20,
      capsulePad: 24,
      logo: '/static/brand-logo.png',
      quicks: [
        { key: 'company', label: '公司注册', icon: '/static/icons/home-company.png', url: '/pages/svc/detail?categoryId=102' },
        { key: 'assess', label: '身份评估', icon: '/static/icons/home-assess.png', url: '/pages/assess/assess' },
        { key: 'book', label: '空间预订', icon: '/static/icons/home-book.png', url: '/pages/space/space' }
      ]
    }
  },
  computed: {
    hotCommunities() {
      return this.communities.slice(0, 3)
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
      this.stories = (d || []).slice(0, 8).map((s, i) => ({
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
      this.communities = (d || []).map(c => ({
        ...c,
        image: c.coverUrl ? mediaUrl(c.coverUrl, BASE) : communityImage(c.name)
      }))
    })
  },
  methods: {
    openQuick(q) {
      this.go(q.url)
    },
    onContactDone(e) {
      const detail = (e && e.detail) || {}
      const code = Number(detail.errcode)
      if (code === 0 || code === -3006) return
      const msg = {
        '-3002': '客服配置读取失败',
        '-3004': '需要授权后才能联系客服',
        '-3005': '客服消息发送失败',
        '-3008': '暂未配置客服'
      }[String(code)]
      if (msg) uni.showToast({ title: msg, icon: 'none' })
    },
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
.home { min-height: 100vh; background: #F2F2F7; padding-bottom: 48rpx; }
.nav { padding-left: 44rpx; padding-bottom: 28rpx; }
.brand-row { display: flex; align-items: center; gap: 16rpx; }
.brand-logo { width: 64rpx; height: 64rpx; flex-shrink: 0; }
.brand { color: #1D1D1F; font-size: 34rpx; font-weight: 600; letter-spacing: -0.5rpx; line-height: 1.2; }
.brand-sub { margin-top: 4rpx; color: #86868B; font-size: 24rpx; line-height: 1.4; }
.banner { height: 360rpx; }
.slide { position: relative; height: 360rpx; margin: 0 16rpx; border-radius: 28rpx; overflow: hidden; }
.slide-img, .slide-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.slide-shade { background: linear-gradient(180deg, rgba(0,0,0,0) 42%, rgba(0,0,0,.55) 100%); }
.slide-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 28rpx 32rpx 48rpx; color: #fff; }
.slide-title { font-size: 34rpx; font-weight: 600; letter-spacing: -0.5rpx; }
.slide-sub { margin-top: 6rpx; font-size: 24rpx; color: rgba(255,255,255,.86); }
.sheet {
  margin: 14rpx 16rpx 0; padding: 28rpx 0 12rpx; background: #fff; border-radius: 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(0,0,0,.04); overflow: hidden;
}
.section-head { display: flex; align-items: baseline; justify-content: space-between; padding-right: 28rpx; }
.section-title { display: block; padding: 0 28rpx 20rpx; color: #1D1D1F; font-size: 34rpx; font-weight: 600; letter-spacing: -0.5rpx; }
.section-head .section-title { padding-bottom: 20rpx; }
.more { color: #007AFF; font-size: 26rpx; }
.quick { display: flex; flex-wrap: wrap; padding: 4rpx 8rpx 8rpx; }
.qi { width: 25%; display: flex; flex-direction: column; align-items: center; margin-bottom: 24rpx; }
.contact { position: relative; }
.contact-hit { position: absolute; left: 0; top: 0; width: 100%; height: 100%; opacity: 0; z-index: 2; }
.qicon {
  width: 104rpx; height: 104rpx; border-radius: 26rpx; overflow: hidden;
  box-shadow: 0 10rpx 18rpx rgba(58, 54, 48, .16);
}
.qimg { width: 104rpx; height: 104rpx; display: block; }
.ql { margin-top: 12rpx; font-size: 24rpx; color: #1D1D1F; }
.places { display: flex; gap: 16rpx; padding: 0 24rpx 20rpx; }
.place { width: 0; flex: 1; }
.place-img { width: 100%; height: 180rpx; border-radius: 18rpx; display: block; background: #E5E5EA; }
.place-name {
  margin-top: 12rpx; font-size: 24rpx; font-weight: 500; color: #1D1D1F; line-height: 1.35;
  overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.place-addr { margin-top: 4rpx; font-size: 22rpx; color: #86868B; }
.stories-clip { width: 100%; overflow: hidden; }
.stories { width: 100%; height: 400rpx; white-space: nowrap; }
.stories-row { display: inline-flex; padding: 0 24rpx 16rpx; }
.story {
  width: 400rpx; margin-right: 16rpx; flex-shrink: 0; white-space: normal;
  background: #F5F5F7; border-radius: 20rpx; overflow: hidden;
}
.story:last-child { margin-right: 0; }
.story-img { width: 400rpx; height: 220rpx; display: block; background: #E5E5EA; }
.story-title {
  padding: 16rpx 18rpx 0; font-size: 26rpx; font-weight: 500; color: #1D1D1F; line-height: 1.4;
  height: 72rpx; overflow: hidden;
}
.story-who { padding: 6rpx 18rpx 16rpx; font-size: 22rpx; color: #86868B; }
.news { margin: 0 8rpx 12rpx; }
.nitem { display: flex; gap: 20rpx; align-items: center; padding: 22rpx 20rpx; }
.nitem + .nitem { border-top: 1rpx solid #F2F2F7; }
.nbody { flex: 1; min-width: 0; }
.ntitle { font-size: 28rpx; font-weight: 500; color: #1D1D1F; line-height: 1.4; }
.ndate { margin-top: 8rpx; font-size: 22rpx; color: #86868B; }
.nimg { width: 128rpx; height: 96rpx; border-radius: 16rpx; flex-shrink: 0; background: #E5E5EA; }
</style>
