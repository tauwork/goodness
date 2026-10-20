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

    <view class="filters">
      <view v-for="f in filters" :key="f.code" class="chip" :class="{ on: category === f.code }"
            @tap="category = f.code">{{ f.label }}</view>
    </view>

    <view class="when">
      <picker mode="date" :value="bookDate" :start="today" @change="e => bookDate = e.detail.value">
        <view class="date">{{ dateLabel }}</view>
      </picker>
      <view class="periods">
        <view v-for="p in periods" :key="p.v" class="period" :class="{ on: period === p.v }"
              @tap="period = p.v">{{ p.label }}</view>
      </view>
    </view>

    <view class="card" v-for="c in filtered" :key="c.id" @tap="open(c)">
      <image class="cover" :src="cover(c)" mode="aspectFill" />
      <view class="body">
        <view class="row">
          <text class="name">{{ c.name }}</text>
          <text class="dist" v-if="distText(c)">{{ distText(c) }}</text>
        </view>
        <view class="addr">{{ c.address }}</view>
        <view class="foot">
          <view class="counts">
            <text v-if="c.deskCount">工位 {{ c.deskCount }}</text>
            <text v-if="c.meetingCount">会议室 {{ c.meetingCount }}</text>
            <text v-if="c.roomCount">房间 {{ c.roomCount }}</text>
          </view>
          <text class="price" v-if="priceOf(c) != null">¥{{ money(priceOf(c)) }}起</text>
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
    const d = new Date()
    const today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    return {
      all: [], loaded: false, keyword: '', city: '',
      category: 'DESK', bookDate: today, today, period: 'ALL',
      lat: null, lng: null,
      filters: [
        { code: 'DESK', label: '工位' },
        { code: 'MEETING', label: '会议室' },
        { code: 'ROOM', label: '房间' }
      ],
      periods: [
        { v: 'ALL', label: '全天' },
        { v: 'AM', label: '上午' },
        { v: 'PM', label: '下午' }
      ]
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
    dateLabel() {
      if (!this.bookDate) return ''
      const [y, m, d] = this.bookDate.split('-')
      const tag = this.bookDate === this.today ? ' 今天' : ''
      return `${Number(m)}月${Number(d)}日${tag}`
    },
    filtered() {
      const kw = (this.keyword || '').trim()
      return this.all.filter(c => {
        if (this.city && c.city !== this.city) return false
        if (this.category === 'DESK' && !(c.deskCount > 0)) return false
        if (this.category === 'MEETING' && !(c.meetingCount > 0)) return false
        if (this.category === 'ROOM' && !(c.roomCount > 0)) return false
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
    priceOf(c) {
      if (this.category === 'DESK') return c.deskPriceFrom
      if (this.category === 'MEETING') return c.meetingPriceFrom
      if (this.category === 'ROOM') return c.roomPriceFrom
      return c.priceFrom
    },
    money(v) {
      const n = Number(v)
      if (Number.isNaN(n)) return v
      const s = n.toFixed(2)
      return s.endsWith('.00') ? String(Math.round(n)) : s
    },
    open(c) {
      const q = [`cid=${c.id}`, `date=${this.bookDate}`, `period=${this.period}`]
      uni.navigateTo({ url: `/pages/space/store?${q.join('&')}` })
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
.filters { display: flex; gap: 16rpx; padding: 24rpx 24rpx 8rpx; }
.chip {
  min-width: 132rpx; text-align: center; padding: 16rpx 28rpx; border-radius: 12rpx;
  background: #fff; border: 1rpx solid #E4DDD2; font-size: 28rpx; color: #1C1915;
}
.chip.on { background: #1C1915; color: #fff; border-color: #1C1915; font-weight: 600; }
.when { display: flex; align-items: center; padding: 18rpx 24rpx 8rpx; gap: 20rpx; }
.date { font-size: 34rpx; font-weight: 600; }
.periods { display: flex; background: #E9E4DB; border-radius: 999rpx; padding: 4rpx; }
.period { padding: 10rpx 26rpx; border-radius: 999rpx; font-size: 24rpx; color: #6F675E; }
.period.on { background: #fff; color: #1C1915; font-weight: 600; }
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
.foot { display: flex; align-items: flex-end; margin-top: auto; padding-top: 12rpx; }
.counts { flex: 1; color: #6F675E; font-size: 22rpx; display: flex; gap: 16rpx; flex-wrap: wrap; }
.price { color: #1C1915; font-size: 30rpx; font-weight: 700; }
.empty, .end { text-align: center; color: #A3988C; font-size: 24rpx; padding: 48rpx 0 20rpx; }
</style>
