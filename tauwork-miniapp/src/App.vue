<script>
export default {
  onLaunch() {
    console.log('九千善小程序启动')
  }
}
</script>

<style>
/* 全局色板（原型 v1.1 体系） */
page {
  background: #F4F6FA;
  font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #22304A;
  font-size: 28rpx;
}
.navy { color: #16355E; }
.gold { color: #A8853F; }
.sub { color: #8A94A8; font-size: 24rpx; }
.card {
  background: #fff; border-radius: 24rpx; margin: 0 24rpx 24rpx;
  padding: 28rpx; box-shadow: 0 2rpx 8rpx rgba(22,53,94,.06);
}
.sect { display: flex; align-items: baseline; padding: 32rpx 32rpx 20rpx; }
.sect h3 { font-size: 32rpx; font-weight: 600; }
.btn-main {
  background: #16355E; color: #fff; border-radius: 44rpx;
  font-size: 30rpx; font-weight: 600; text-align: center; padding: 22rpx 0;
}
.btn-gold {
  background: linear-gradient(135deg,#C2A05F,#A8853F); color: #fff;
  border-radius: 44rpx; font-size: 30rpx; font-weight: 600; text-align: center; padding: 22rpx 0;
}
.tag {
  font-size: 20rpx; border-radius: 8rpx; padding: 4rpx 14rpx;
  color: #16355E; background: #EDF2F9; display: inline-block; margin-right: 10rpx;
}
.tag-g { color: #A8853F; background: #F7F1E2; }
</style>
