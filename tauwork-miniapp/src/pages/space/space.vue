<template>
  <view class="page">
    <view class="search">
      <picker mode="selector" :range="cities" :value="cityIndex" @change="onCity">
        <view class="city">
          <text class="pin"></text>
          <text>{{ city || '全部' }}</text>
          <text class="caret">▾</text>
        </view>
      </picker>
      <view class="search-line"></view>
      <input class="search-input" v-model="keyword" confirm-type="search" placeholder="搜索社区名称、地址" />
    </view>

    <view class="card" v-for="c in filtered" :key="c.id" @tap="open(c)">
      <image class="cover" :src="cover(c)" mode="aspectFill" />
      <view class="body">
        <view class="row">
          <text class="name">{{ c.name }}</text>
          <text class="dist" v-if="distText(c)">{{ distText(c) }}</text>
        </view>
        <view class="addr">{{ c.address }}</view>
        <view class="pgroup" v-if="ofKind(c, 'DESK').length">
          <text class="plabel">工位</text>
          <view class="pitem" v-for="p in ofKind(c, 'DESK')" :key="p.communityProductId">
            <text class="pname">{{ p.name }}</text>
            <text class="pprice" v-if="p.price != null">¥{{ money(p.price) }}<text class="punit">/{{ unitText(p.priceUnit) }}</text></text>
          </view>
        </view>
        <view class="pgroup" v-if="ofKind(c, 'MEETING').length">
          <text class="plabel">会议室</text>
          <view class="pitem" v-for="p in ofKind(c, 'MEETING')" :key="p.communityProductId">
            <text class="pname">{{ p.name }}</text>
            <text class="pprice" v-if="p.price != null">¥{{ money(p.price) }}<text class="punit">/{{ unitText(p.priceUnit) }}</text></text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="loaded && !filtered.length" class="empty">这个条件下还没有社区</view>
    <view v-else-if="loaded" class="end">已显示全部社区</view>
  </view>
</template>

<script>
import api, { BASE } from '../../utils/request'
import { communityImage, mediaUrl } from '../../utils/visual'

export default {
  data() {
    return {
      all: [], loaded: false, keyword: '', city: '',
      lat: null, lng: null,
      offers: {}
    }
  },
  computed: {
    cities() {
      return [...new Set(this.all.map(c => c.city).filter(Boolean))]
    },
    cityIndex() {
      const i = this.cities.indexOf(this.city)
      return i < 0 ? 0 : i
    },
    filtered() {
      const kw = (this.keyword || '').trim()
      return this.all.filter(c => {
        if (this.city && c.city !== this.city) return false
        if (kw && !`${c.name || ''}${c.address || ''}${c.district || ''}`.includes(kw)) return false
        return true
      })
    }
  },
  onShow() {
    if (this.triedLocate) {
      this.load()
      return
    }
    this.triedLocate = true
    this.locate()
  },
  methods: {
    locate() {
      uni.getLocation({
        type: 'gcj02',
        success: res => {
          this.lat = res.latitude
          this.lng = res.longitude
          this.load()
        },
        fail: () => this.load()
      })
    },
    load() {
      const q = {}
      if (this.lat != null) {
        q.lat = this.lat
        q.lng = this.lng
      }
      api.get('/api/mini/space/community-card', q).then(d => {
        this.all = d || []
        if (!this.city) {
          const nearest = this.all.find(c => c.distanceKm != null) || this.all[0]
          this.city = nearest ? nearest.city : ''
        }
        return Promise.all(this.all.map(c =>
          api.get(`/api/mini/space/community/${c.id}/product`).then(list => {
            this.offers[String(c.id)] = list || []
          }).catch(() => { this.offers[String(c.id)] = [] })
        ))
      }).finally(() => { this.loaded = true })
    },
    onCity(e) {
      this.city = this.cities[Number(e.detail.value)] || ''
    },
    cover(c) {
      return c.coverUrl ? mediaUrl(c.coverUrl, BASE) : communityImage(c.name)
    },
    distText(c) {
      if (c.distanceKm == null) return ''
      if (c.distanceKm >= 10) return '>10km'
      if (c.distanceKm < 1) return `${Math.max(1, Math.round(c.distanceKm * 1000))}m`
      return `${c.distanceKm.toFixed(1)}km`
    },
    ofKind(c, code) {
      return (this.offers[String(c.id)] || []).filter(p => p.categoryCode === code)
    },
    money(v) {
      const n = Number(v)
      if (Number.isNaN(n)) return v
      const s = n.toFixed(2)
      return s.endsWith('.00') ? String(Math.round(n)) : s
    },
    unitText(u) {
      return { HOUR: '小时', DAY: '天', MONTH: '月', ONCE: '场' }[u] || '次'
    },
    open(c) {
      uni.navigateTo({ url: `/pages/space/store?cid=${c.id}` })
    }
  }
}
</script>

<style scoped>
.page { padding: 16rpx 0 40rpx; }
.search {
  display: flex; align-items: center; margin: 8rpx 24rpx 0;
  background: #fff; border-radius: 20rpx; padding: 8rpx 8rpx 8rpx 20rpx;
  border: 1rpx solid rgba(28,25,21,.06);
}
.city { display: flex; align-items: center; gap: 8rpx; font-size: 28rpx; font-weight: 600; padding: 16rpx 8rpx; }
.pin {
  width: 22rpx; height: 22rpx; border: 4rpx solid #1C1915; border-radius: 50% 50% 50% 0;
  transform: rotate(-45deg); margin-right: 6rpx;
}
.caret { color: #8A8175; font-size: 22rpx; }
.search-line { width: 1rpx; height: 32rpx; background: #E6DFD4; margin: 0 12rpx; }
.search-input { flex: 1; font-size: 26rpx; height: 72rpx; }
.card {
  display: flex; gap: 20rpx; margin: 20rpx 24rpx 0; padding: 20rpx;
  background: #fff; border-radius: 24rpx; border: 1rpx solid rgba(28,25,21,.05);
}
.cover { width: 168rpx; height: 168rpx; border-radius: 16rpx; flex-shrink: 0; }
.body { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.row { display: flex; align-items: flex-start; gap: 12rpx; }
.name { flex: 1; font-size: 30rpx; font-weight: 600; line-height: 1.35; }
.dist { color: #8A8175; font-size: 22rpx; flex-shrink: 0; margin-top: 4rpx; }
.addr { color: #8A8175; font-size: 22rpx; margin-top: 8rpx; line-height: 1.4; }
.pgroup { margin-top: 12rpx; }
.plabel { display: block; font-size: 22rpx; font-weight: 600; color: #1C1915; }
.pitem { display: flex; align-items: baseline; gap: 12rpx; margin-top: 6rpx; }
.pname { flex: 1; min-width: 0; font-size: 22rpx; color: #6F675E; line-height: 1.45; }
.pprice { flex-shrink: 0; font-size: 22rpx; font-weight: 600; color: #1C1915; }
.punit { font-size: 20rpx; font-weight: 400; color: #8A8175; }
.empty, .end { text-align: center; color: #A3988C; font-size: 24rpx; padding: 48rpx 0 20rpx; }
</style>
