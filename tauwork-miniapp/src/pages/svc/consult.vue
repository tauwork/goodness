<template>
  <view class="page">
    <view class="intro">
      <view class="intro-title">免费咨询</view>
      <view class="intro-desc">选择分类后可直接联系顾问，也可以留下联系方式。</view>
    </view>

    <view class="sheet">
      <view class="tabs">
        <view class="tab" v-for="g in groups" :key="g.code" :class="{ on: group === g.code }" @tap="group = g.code">
          <text class="tab-name">{{ g.name }}</text>
          <view class="tab-bar"></view>
        </view>
      </view>
      <view class="panel" v-if="current">
        <view class="contact">
          <view class="contact-title">{{ current.consultant && current.consultant.name ? current.consultant.name : '顾问' }}</view>
          <view class="contact-title-sub" v-if="current.consultant && current.consultant.title">{{ current.consultant.title }}</view>
          <view class="contact-row" v-if="current.consultant && current.consultant.wechat">
            <text class="k">微信</text>
            <text class="v">{{ current.consultant.wechat }}</text>
            <text class="act" @tap.stop="copy(current.consultant.wechat)">复制</text>
          </view>
          <view class="contact-row" v-if="current.consultant && current.consultant.phone">
            <text class="k">电话</text>
            <text class="v">{{ current.consultant.phone }}</text>
            <text class="act" @tap.stop="call(current.consultant.phone)">拨打</text>
          </view>
          <view class="contact-empty" v-if="group === 'ALL'">选择具体分类后可查看顾问</view>
          <view class="contact-empty" v-else-if="!hasContact">顾问联系方式待配置</view>
        </view>
      </view>
    </view>

    <view class="sheet form">
      <view class="field">
        <text class="label">姓名</text>
        <input class="inp" v-model="form.name" maxlength="32" placeholder="怎么称呼您" />
      </view>
      <view class="field">
        <text class="label">电话</text>
        <input class="inp" v-model="form.phone" maxlength="11" type="number" placeholder="方便顾问联系您" />
      </view>
      <view class="field">
        <text class="label">需求描述</text>
        <textarea class="inp ta" v-model="form.remark" maxlength="500" placeholder="简单说说您想咨询的问题" />
      </view>
      <view class="submit" :class="{ off: sending }" @tap="submit">提交预约</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return {
      groups: [],
      group: 'ALL',
      loaded: false,
      sending: false,
      form: { name: '', phone: '', remark: '' }
    }
  },
  computed: {
    current() {
      return this.groups.find(g => g.code === this.group) || null
    },
    hasContact() {
      const c = this.current && this.current.consultant
      return !!(c && (c.wechat || c.phone))
    }
  },
  onLoad(q) {
    if (q && q.group) this.group = q.group
  },
  onShow() {
    this.load()
  },
  methods: {
    same(a, b) {
      return a != null && String(a) === String(b)
    },
    load() {
      api.get('/api/mini/svc/consult').then(d => {
        this.groups = (d && d.groups) || []
        if (!this.groups.some(g => g.code === this.group)) this.group = 'ALL'
      }).finally(() => { this.loaded = true })
    },
    copy(text) {
      uni.setClipboardData({ data: text, success: () => uni.showToast({ title: '微信号已复制', icon: 'none' }) })
    },
    call(phone) {
      uni.makePhoneCall({ phoneNumber: String(phone) })
    },
    async submit() {
      if (this.sending) return
      const name = (this.form.name || '').trim()
      const phone = (this.form.phone || '').trim()
      if (!this.current) return uni.showToast({ title: '请选择服务分类', icon: 'none' })
      if (!name) return uni.showToast({ title: '请填写姓名', icon: 'none' })
      if (!/^1\d{10}$/.test(phone)) return uni.showToast({ title: '请填写正确的手机号', icon: 'none' })
      this.sending = true
      try {
        await api.post('/api/mini/svc/consult', {
          group: this.current.code,
          name,
          phone,
          remark: (this.form.remark || '').trim() || null
        }, false)
        this.form = { name: '', phone: '', remark: '' }
        uni.showModal({
          title: '预约已提交',
          content: '顾问会尽快与您联系',
          showCancel: false
        })
      } catch (e) {
      } finally {
        this.sending = false
      }
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F2F2F7; padding-bottom: 48rpx; }
.intro { padding: 28rpx 32rpx 8rpx; }
.intro-title { color: #1D1D1F; font-size: 40rpx; font-weight: 600; letter-spacing: -0.5rpx; }
.intro-desc { color: #86868B; font-size: 26rpx; line-height: 1.6; margin-top: 10rpx; }
.sheet {
  margin: 20rpx 16rpx 0; background: #fff; border-radius: 28rpx; overflow: hidden;
  box-shadow: 0 12rpx 32rpx rgba(0,0,0,.04);
}
.tabs { display: flex; flex-wrap: wrap; align-items: flex-start; padding: 20rpx 8rpx 0; }
.tab { width: 25%; display: flex; flex-direction: column; align-items: center; padding-bottom: 14rpx; }
.tab-name {
  width: 100%; text-align: center; font-size: 24rpx; color: #86868B; line-height: 1.3;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.tab.on .tab-name { color: #1D1D1F; font-weight: 600; }
.tab-bar { width: 36rpx; height: 4rpx; margin-top: 10rpx; border-radius: 4rpx; background: transparent; }
.tab.on .tab-bar { background: #1D1D1F; }
.panel { border-top: 1rpx solid #F2F2F7; }
.contact { margin: 20rpx 24rpx 24rpx; padding: 20rpx 22rpx; background: #F5F5F7; border-radius: 20rpx; }
.contact-title { font-size: 28rpx; font-weight: 600; color: #1D1D1F; }
.contact-title-sub { margin-top: 4rpx; font-size: 22rpx; color: #86868B; }
.contact-row { display: flex; align-items: center; margin-top: 14rpx; }
.k { width: 72rpx; font-size: 24rpx; color: #86868B; }
.v { flex: 1; min-width: 0; font-size: 26rpx; color: #1D1D1F; }
.act { font-size: 24rpx; color: #007AFF; padding-left: 16rpx; }
.contact-empty, .empty { text-align: center; color: #86868B; font-size: 24rpx; padding: 28rpx 24rpx; }
.form { padding: 8rpx 24rpx 28rpx; }
.field { padding-top: 20rpx; }
.label { display: block; font-size: 24rpx; color: #86868B; margin-bottom: 10rpx; }
.inp {
  width: 100%; box-sizing: border-box; background: #F5F5F7; border-radius: 16rpx;
  padding: 22rpx 24rpx; font-size: 28rpx; color: #1D1D1F;
}
.ta { min-height: 160rpx; }
.submit {
  margin-top: 28rpx; height: 88rpx; line-height: 88rpx; text-align: center;
  background: #1D1D1F; color: #fff; border-radius: 999rpx; font-size: 30rpx; font-weight: 500;
}
.submit.off { opacity: .5; }
</style>
