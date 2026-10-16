<template>
  <view class="page">
    <view class="head">
      <image class="avatar" src="/static/logo.png" mode="aspectFit" />
      <view class="hinfo">
        <view class="hname">九千善客户</view>
        <view class="hsub">登录后可预订空间、查看入场码</view>
      </view>
    </view>

    <view class="menu card">
      <view class="mitem" @tap="go('/pages/me/bookings')">
        <text class="mic">📋</text><text>我的预订</text><text class="arrow">›</text>
      </view>
      <view class="mitem" @tap="reLogin">
        <text class="mic">🔄</text><text>重新登录（切换身份）</text><text class="arrow">›</text>
      </view>
      <view class="mitem" @tap="callService">
        <text class="mic">📞</text><text>联系客服</text><text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
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
.head { background: linear-gradient(160deg,#16355E,#0E2544); padding: 40rpx 32rpx 60rpx;
  display: flex; align-items: center; gap: 22rpx; }
.avatar { width: 110rpx; height: 110rpx; border-radius: 50%; background: #2E5C9E; }
.hname { color: #fff; font-size: 34rpx; font-weight: 600; }
.hsub { color: #9DB2D2; font-size: 22rpx; margin-top: 8rpx; }
.menu { margin-top: -30rpx; padding: 6rpx 28rpx; }
.mitem { display: flex; align-items: center; gap: 18rpx; padding: 30rpx 0;
  border-bottom: 1rpx solid #F1F4F8; font-size: 28rpx; }
.mitem:last-child { border: none; }
.mic { font-size: 32rpx; }
.arrow { margin-left: auto; color: #C3CBD8; }
</style>
