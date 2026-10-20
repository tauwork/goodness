<script>
import { capturePartner } from './utils/request'

export default {
  onLaunch(options) {
    capturePartner(options)
  },
  onShow(options) {
    capturePartner(options)
  }
}
</script>

<style>
page {
  background: #F3EEE6;
  font-family: -apple-system, "PingFang SC", "Hiragino Sans GB", sans-serif;
  color: #1C1915;
  font-size: 28rpx;
}
.kicker {
  font-size: 20rpx;
  letter-spacing: 4rpx;
  color: #9A7B4F;
  font-weight: 500;
}
.sub { color: #8A8175; font-size: 24rpx; line-height: 1.6; }
.card {
  background: #FFFcf8;
  border-radius: 24rpx;
  margin: 0 28rpx 24rpx;
  padding: 32rpx;
  border: 1rpx solid rgba(28, 25, 21, 0.06);
}
.btn-main, .btn-gold {
  background: #1C1915;
  color: #F6E7C8;
  border-radius: 999rpx;
  font-size: 28rpx;
  letter-spacing: 2rpx;
  text-align: center;
  padding: 26rpx 0;
}
.tag {
  font-size: 20rpx;
  letter-spacing: 1rpx;
  border-radius: 999rpx;
  padding: 6rpx 16rpx;
  color: #6F6252;
  background: #F3EEE6;
  display: inline-block;
  margin-right: 10rpx;
}
.sink { padding-bottom: 180rpx; }
.paybar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  background: rgba(251, 248, 243, 0.96);
  border-top: 1rpx solid #E6DFD4;
  display: flex;
  align-items: center;
  padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));
}
.paybtn { flex: 1; }
.pamount { color: #8C6A3D; font-size: 40rpx; font-weight: 600; margin-right: 28rpx; }
.empty-box {
  margin: 12rpx 28rpx 28rpx;
  background: #FFFcf8;
  border-radius: 24rpx;
  overflow: hidden;
  border: 1rpx solid rgba(28, 25, 21, 0.06);
}
.empty-box image { width: 100%; height: 240rpx; display: block; }
.empty-box text { display: block; text-align: center; color: #8A8175; padding: 28rpx; font-size: 26rpx; }
</style>
