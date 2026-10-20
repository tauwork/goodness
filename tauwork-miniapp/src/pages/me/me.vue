<template>
  <view class="page">
    <view class="cover">
      <image class="cover-img" src="/static/visual/harbor.jpg" mode="aspectFill" />
      <view class="cover-shade"></view>
      <view class="who">
        <image class="avatar" src="/static/logo.png" mode="aspectFill" />
        <view>
          <view class="who-en">MEMBER</view>
          <view class="who-name">九千善客户</view>
          <view class="who-sub">预订社区、查看入场码、跟进顾问</view>
        </view>
      </view>
    </view>

    <view class="menu">
      <view class="mitem" v-for="m in menus" :key="m.url" @tap="go(m.url)">
        <image class="mico" :src="m.icon" mode="aspectFit" />
        <view class="mbody">
          <view class="mtitle">{{ m.title }}</view>
          <view class="msub">{{ m.sub }}</view>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="mitem" @tap="reLogin">
        <image class="mico" src="/static/icons/refresh.png" mode="aspectFit" />
        <view class="mbody">
          <view class="mtitle">重新登录</view>
          <view class="msub">切换当前微信身份</view>
        </view>
        <text class="arrow">›</text>
      </view>
      <view class="mitem" @tap="callService">
        <image class="mico" src="/static/icons/service.png" mode="aspectFit" />
        <view class="mbody">
          <view class="mtitle">联系客服</view>
          <view class="msub">工作时间电话沟通</view>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return {
      menus: [
        { icon: '/static/icons/calendar.png', title: '我的预订', sub: '社区、咨询与商品订单', url: '/pages/me/bookings' },
        { icon: '/static/icons/phone.png', title: '绑定手机号', sub: '顾问回访与进度通知', url: '/pages/me/phone' },
        { icon: '/static/icons/family.png', title: '家庭档案', sub: '为评估准备家庭情况', url: '/pages/me/family' },
        { icon: '/static/icons/share.png', title: '合伙人海报', sub: '邀请好友成为合伙人', url: '/pages/me/partner-poster' },
        { icon: '/static/icons/coin.png', title: '我的佣金', sub: '直接、间接和可提现余额', url: '/pages/me/partner-commission' },
        { icon: '/static/icons/family.png', title: '我的客户', sub: '直接客户和间接客户', url: '/pages/me/partner-customers' }
      ]
    }
  },
  methods: {
    go(url) { uni.navigateTo({ url }) },
    async reLogin() {
      uni.removeStorageSync('token')
      await api.ensureLogin()
      uni.showToast({ title: '已登录', icon: 'success' })
    },
    callService() {
      uni.makePhoneCall({ phoneNumber: '0755-0000000', fail: () => {} })
    }
  }
}
</script>

<style scoped>
.cover { position: relative; height: 360rpx; margin: 20rpx 28rpx 0; border-radius: 28rpx; overflow: hidden; }
.cover-img, .cover-shade { position: absolute; left: 0; top: 0; width: 100%; height: 100%; }
.cover-shade { background: linear-gradient(180deg, rgba(16,14,12,.1), rgba(16,14,12,.62)); }
.who { position: absolute; left: 28rpx; right: 28rpx; bottom: 28rpx; display: flex; align-items: center; gap: 20rpx; color: #fff; }
.avatar { width: 96rpx; height: 96rpx; border-radius: 24rpx; }
.who-en { font-size: 18rpx; letter-spacing: 4rpx; color: #E7D3AE; }
.who-name { font-size: 36rpx; font-weight: 500; margin-top: 4rpx; }
.who-sub { font-size: 22rpx; color: rgba(255,255,255,.8); margin-top: 6rpx; }
.menu { margin: 24rpx 28rpx; background: #FFFcf8; border-radius: 28rpx; overflow: hidden; border: 1rpx solid rgba(28,25,21,.06); }
.mitem { display: flex; align-items: center; gap: 20rpx; padding: 26rpx 28rpx; }
.mitem + .mitem { border-top: 1rpx solid #F0EBE3; }
.mico { width: 40rpx; height: 40rpx; flex-shrink: 0; }
.mbody { flex: 1; }
.mtitle { font-size: 30rpx; }
.msub { font-size: 22rpx; color: #8A8175; margin-top: 4rpx; }
.arrow { color: #C4BBAF; font-size: 36rpx; }
</style>
