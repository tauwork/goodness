<template>
  <view class="page" v-if="o">
    <view class="sheet">
      <view class="status">{{ statusText(o.status) }}</view>
      <view class="name">{{ o.productName }}</view>
      <view class="row"><text>订单号</text><text class="val">{{ o.orderNo }}</text></view>
      <view class="row"><text>应付金额</text><text class="val">¥{{ money(o.amount) }}</text></view>
      <view class="row"><text>支付方式</text><text class="val">{{ channelText(o.payChannel) }}</text></view>
      <view class="row"><text>下单时间</text><text class="val">{{ (o.createTime || '').replace('T', ' ').slice(0, 16) }}</text></view>
    </view>

    <view class="sheet" v-if="o.payChannel === 'TRANSFER' && o.status !== 'CANCELED' && o.status !== 'PAID'">
      <view class="block-title">收款账户</view>
      <view class="row"><text>户名</text><text class="val">{{ showAccount(account.accountName) }}</text></view>
      <view class="row"><text>开户行</text><text class="val">{{ showAccount(account.bankName) }}</text></view>
      <view class="row" v-if="account.branch"><text>支行</text><text class="val">{{ account.branch }}</text></view>
      <view class="row"><text>账号</text><text class="val">{{ showAccount(account.accountNo) }}</text></view>
      <view class="tip">转账时请备注订单号。上传凭证后，到账会尽快确认。</view>
      <view class="btn" @tap="chooseVoucher">{{ o.voucherUrl ? '重新上传凭证' : '上传凭证' }}</view>
    </view>

    <view class="sheet" v-if="o.voucherUrl">
      <view class="block-title">转账凭证</view>
      <image class="voucher" :src="voucher" mode="widthFix" />
    </view>

    <view class="bar" v-if="o.payChannel === 'WECHAT' && o.status === 'PENDING_PAY'">
      <view class="btn solid" @tap="continuePay">继续支付</view>
    </view>
    <view class="bar" v-else-if="o.status === 'PENDING_PAY'">
      <view class="btn ghost" @tap="cancel">取消订单</view>
    </view>
  </view>
</template>

<script>
import api, { BASE, ensureLogin } from '../../utils/request'
import { mediaUrl } from '../../utils/visual'

export default {
  data() {
    return { o: null, account: {} }
  },
  computed: {
    voucher() {
      return this.o && this.o.voucherUrl ? mediaUrl(this.o.voucherUrl, BASE) : ''
    }
  },
  onLoad(q) {
    this.no = q.no
  },
  onShow() {
    if (this.no) this.load()
  },
  methods: {
    load() {
      api.get(`/api/mini/svc-order/${this.no}`, null, true).then(d => {
        this.o = d
        this.account = (d && d.account) || {}
      })
    },
    money(n) {
      const num = Number(n)
      if (Number.isNaN(num)) return n
      return Number.isInteger(num) ? String(num) : num.toFixed(2)
    },
    showAccount(text) { return text ? text : '待配置' },
    statusText(s) {
      return { PENDING_PAY: '待支付', CONFIRMING: '待确认到账', PAID: '已支付', CANCELED: '已取消' }[s] || s
    },
    channelText(s) {
      return { WECHAT: '在线支付', TRANSFER: '对公转账' }[s] || s
    },
    continuePay() {
      api.post(`/api/mini/svc-order/${this.o.orderNo}/pay`, {}, true).then(order => {
        if (order.mock || !(order.payParams && order.payParams.paySign)) {
          uni.showModal({
            title: '确认支付',
            content: `${order.orderNo}\n¥${this.money(order.amount)}`,
            success: res => {
              if (!res.confirm) return
              api.post(`/api/mini/svc-order/mock-pay/${order.orderNo}`, {}, true).then(() => {
                uni.showToast({ title: '支付成功', icon: 'none' })
                this.load()
              })
            }
          })
          return
        }
        const params = order.payParams
        uni.requestPayment({
          provider: 'wxpay',
          timeStamp: params.timeStamp,
          nonceStr: params.nonceStr,
          package: params.package,
          signType: params.signType || 'RSA',
          paySign: params.paySign,
          success: () => { uni.showToast({ title: '支付成功', icon: 'none' }); this.load() },
          fail: () => uni.showToast({ title: '支付未完成', icon: 'none' })
        })
      })
    },
    cancel() {
      uni.showModal({
        title: '取消订单',
        content: '确认取消这笔待支付订单？',
        success: res => {
          if (!res.confirm) return
          api.put(`/api/mini/svc-order/${this.o.orderNo}/cancel`, {}, true).then(() => {
            uni.showToast({ title: '已取消', icon: 'none' })
            this.load()
          })
        }
      })
    },
    async chooseVoucher() {
      if (!uni.getStorageSync('token')) await ensureLogin()
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: res => {
          uni.uploadFile({
            url: BASE + '/api/mini/file/upload',
            filePath: res.tempFilePaths[0],
            name: 'file',
            header: { Authorization: uni.getStorageSync('token') || '' },
            success: up => {
              let body = {}
              try { body = JSON.parse(up.data) } catch (e) {}
              if (body.code !== 0 || !body.data) {
                uni.showToast({ title: body.msg || '上传失败', icon: 'none' })
                return
              }
              api.post(`/api/mini/svc-order/${this.o.orderNo}/voucher`, { url: body.data.url }, true).then(() => {
                uni.showToast({ title: '凭证已提交', icon: 'none' })
                this.load()
              })
            },
            fail: () => uni.showToast({ title: '上传失败', icon: 'none' })
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F2F2F7; padding: 16rpx 16rpx 180rpx; }
.sheet { background: #fff; border-radius: 28rpx; padding: 32rpx; margin-bottom: 16rpx; }
.status { color: #86868B; font-size: 24rpx; }
.name { margin-top: 8rpx; color: #1D1D1F; font-size: 36rpx; font-weight: 600; line-height: 1.4; }
.block-title { color: #1D1D1F; font-size: 30rpx; font-weight: 600; margin-bottom: 8rpx; }
.row { display: flex; justify-content: space-between; gap: 24rpx; padding: 20rpx 0; border-top: 1rpx solid #F2F2F7; font-size: 26rpx; color: #86868B; }
.val { color: #1D1D1F; text-align: right; flex: 1; }
.tip { margin-top: 12rpx; color: #86868B; font-size: 24rpx; line-height: 1.5; }
.voucher { width: 100%; margin-top: 12rpx; border-radius: 16rpx; }
.btn, .solid, .ghost {
  margin-top: 24rpx; height: 88rpx; line-height: 88rpx; text-align: center; border-radius: 999rpx; font-size: 30rpx;
}
.btn, .solid { background: #1D1D1F; color: #fff; }
.ghost { background: #F2F2F7; color: #1D1D1F; }
.bar { position: fixed; left: 0; right: 0; bottom: 0; padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom)); background: #fff; }
.bar .btn, .bar .solid, .bar .ghost { margin-top: 0; }
</style>
