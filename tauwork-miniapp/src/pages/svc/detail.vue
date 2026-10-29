<template>
  <view class="page" v-if="p">
    <image class="hero" :src="image" mode="aspectFill" />
    <view class="sheet">
      <view class="name">{{ p.name }}</view>
      <view class="desc" v-if="desc">{{ desc }}</view>
      <view class="price">¥{{ money(p.priceValue) }}<text v-if="p.priceMode === 'STAGED'" class="from"> 起</text></view>
    </view>

    <view class="bar">
      <view class="btn ghost contact">
        1V1咨询
        <cell
          class="contact-hit"
          plugid="790fd0f846a7e2ac117c8c1bee554bb1"
          styleType="2"
          blockStyle="button"
          buttonStyle="light"
          @completemessage="onContactDone"
        />
      </view>
      <view class="btn solid" @tap="openOrder">立即下单</view>
    </view>

    <view class="mask" v-if="sheet" @tap="closeSheet">
      <view class="panel" @tap.stop>
        <view class="panel-title">{{ sheet === 'transfer' ? '对公转账' : '订单信息' }}</view>
        <view class="row"><text>服务名称</text><text class="val">{{ p.name }}</text></view>
        <view class="row"><text>应付金额</text><text class="val price-inline">¥{{ money(p.priceValue) }}</text></view>
        <view class="row" v-if="orderNo"><text>订单号</text><text class="val">{{ orderNo }}</text></view>

        <block v-if="sheet === 'order'">
          <view class="actions">
            <view class="btn solid" @tap="payOnline">在线支付</view>
            <view class="btn ghost" @tap="payTransfer">对公转账</view>
          </view>
        </block>

        <block v-if="sheet === 'transfer'">
          <view class="bank">
            <view class="bank-line"><text>户名</text><text>{{ showAccount(account.accountName) }}</text></view>
            <view class="bank-line"><text>开户行</text><text>{{ showAccount(account.bankName) }}</text></view>
            <view class="bank-line" v-if="account.branch"><text>支行</text><text>{{ account.branch }}</text></view>
            <view class="bank-line"><text>账号</text><text>{{ showAccount(account.accountNo) }}</text></view>
            <view class="bank-tip">转账时请备注订单号，上传凭证后即可提交确认。</view>
          </view>
          <view class="btn solid" @tap="chooseVoucher">上传凭证</view>
        </block>
      </view>
    </view>
  </view>
  <view v-else-if="missing" class="page empty">暂无该服务</view>
</template>

<script>
import api, { BASE, ensureLogin } from '../../utils/request'
import { serviceImage, mediaUrl } from '../../utils/visual'

export default {
  data() {
    return {
      p: null,
      missing: false,
      sheet: '',
      orderNo: '',
      paying: false,
      account: {}
    }
  },
  computed: {
    image() {
      if (!this.p) return ''
      return this.p.coverUrl ? mediaUrl(this.p.coverUrl, BASE) : serviceImage(this.p)
    },
    desc() {
      if (!this.p) return ''
      return this.p.detail || this.p.subtitle || ''
    }
  },
  onLoad(q) {
    if (q.id) {
      api.get(`/api/mini/svc/product/${q.id}`).then(p => { this.p = p }).catch(() => { this.missing = true })
      return
    }
    if (q.categoryId) {
      api.get('/api/mini/svc/product', { categoryId: q.categoryId }).then(list => {
        const rows = list || []
        const hit = rows.find(item => (item.name || '').includes('公司注册')) || rows[0]
        if (!hit) this.missing = true
        else this.p = hit
      }).catch(() => { this.missing = true })
    }
  },
  methods: {
    money(n) {
      if (n == null || n === '') return '--'
      const num = Number(n)
      return Number.isInteger(num) ? String(num) : num.toFixed(2)
    },
    showAccount(text) {
      return text ? text : '待配置'
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
    openOrder() {
      if (this.p.priceValue == null) return uni.showToast({ title: '服务未标价', icon: 'none' })
      this.sheet = 'order'
    },
    closeSheet() {
      if (this.paying) return
      this.sheet = ''
    },
    async payOnline() {
      if (this.paying) return
      this.paying = true
      try {
        const order = await api.post('/api/mini/svc-order', {
          productId: this.p.id,
          payChannel: 'WECHAT'
        }, true)
        this.orderNo = order.orderNo
        this.launchPay(order)
      } catch (e) {
        this.paying = false
      }
    },
    launchPay(order) {
      const params = order.payParams || {}
      if (order.mock || !params.paySign) {
        uni.showModal({
          title: '确认支付',
          content: `${order.orderNo}\n¥${this.money(order.amount)}`,
          success: res => {
            this.paying = false
            if (!res.confirm) {
              uni.navigateTo({ url: `/pages/me/order?no=${order.orderNo}` })
              return
            }
            api.post(`/api/mini/svc-order/mock-pay/${order.orderNo}`, {}, true).then(() => {
              uni.showToast({ title: '支付成功', icon: 'none' })
              setTimeout(() => uni.redirectTo({ url: `/pages/me/order?no=${order.orderNo}` }), 400)
            })
          }
        })
        return
      }
      uni.requestPayment({
        provider: 'wxpay',
        timeStamp: params.timeStamp,
        nonceStr: params.nonceStr,
        package: params.package,
        signType: params.signType || 'RSA',
        paySign: params.paySign,
        success: () => {
          this.paying = false
          uni.showToast({ title: '支付成功', icon: 'none' })
          setTimeout(() => uni.redirectTo({ url: `/pages/me/order?no=${order.orderNo}` }), 400)
        },
        fail: () => {
          this.paying = false
          uni.showToast({ title: '支付未完成', icon: 'none' })
          setTimeout(() => uni.navigateTo({ url: `/pages/me/order?no=${order.orderNo}` }), 400)
        }
      })
    },
    async payTransfer() {
      if (this.paying) return
      this.paying = true
      try {
        const order = await api.post('/api/mini/svc-order', {
          productId: this.p.id,
          payChannel: 'TRANSFER'
        }, true)
        this.orderNo = order.orderNo
        this.account = order.account || {}
        this.sheet = 'transfer'
      } catch (e) {
      } finally {
        this.paying = false
      }
    },
    async chooseVoucher() {
      if (!this.orderNo) return
      if (!uni.getStorageSync('token')) await ensureLogin()
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: res => this.uploadVoucher(res.tempFilePaths[0])
      })
    },
    uploadVoucher(filePath) {
      uni.uploadFile({
        url: BASE + '/api/mini/file/upload',
        filePath,
        name: 'file',
        header: { Authorization: uni.getStorageSync('token') || '' },
        success: res => {
          let body = {}
          try { body = JSON.parse(res.data) } catch (e) {}
          if (body.code !== 0 || !body.data) {
            uni.showToast({ title: body.msg || '上传失败', icon: 'none' })
            return
          }
          api.post(`/api/mini/svc-order/${this.orderNo}/voucher`, { url: body.data.url }, true).then(() => {
            uni.showToast({ title: '凭证已提交', icon: 'none' })
            setTimeout(() => uni.redirectTo({ url: `/pages/me/order?no=${this.orderNo}` }), 400)
          })
        },
        fail: () => uni.showToast({ title: '上传失败', icon: 'none' })
      })
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #F2F2F7; padding-bottom: 180rpx; }
.hero { width: 100%; height: 520rpx; display: block; background: #E5E5EA; }
.sheet {
  margin: -36rpx 16rpx 0; position: relative; background: #fff; border-radius: 28rpx;
  padding: 36rpx 32rpx 40rpx; box-shadow: 0 12rpx 32rpx rgba(0,0,0,.04);
}
.name { color: #1D1D1F; font-size: 40rpx; font-weight: 600; letter-spacing: -0.5rpx; line-height: 1.35; }
.desc { margin-top: 16rpx; color: #3A3A3C; font-size: 28rpx; line-height: 1.7; }
.price { margin-top: 24rpx; color: #1D1D1F; font-size: 40rpx; font-weight: 600; }
.from { font-size: 24rpx; font-weight: 400; color: #86868B; }
.bar {
  position: fixed; left: 0; right: 0; bottom: 0; display: flex; gap: 16rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: rgba(255,255,255,.96); box-shadow: 0 -8rpx 24rpx rgba(0,0,0,.04);
}
.btn { flex: 1; height: 88rpx; line-height: 88rpx; text-align: center; border-radius: 999rpx; font-size: 30rpx; font-weight: 500; }
.contact { position: relative; }
.contact-hit { position: absolute; left: 0; top: 0; width: 100%; height: 100%; opacity: 0; z-index: 2; }
.solid { background: #1D1D1F; color: #fff; }
.ghost { background: #F2F2F7; color: #1D1D1F; }
.mask { position: fixed; left: 0; right: 0; top: 0; bottom: 0; background: rgba(0,0,0,.4); z-index: 20; display: flex; align-items: flex-end; }
.panel { width: 100%; background: #fff; border-radius: 28rpx 28rpx 0 0; padding: 36rpx 32rpx calc(28rpx + env(safe-area-inset-bottom)); }
.panel-title { font-size: 34rpx; font-weight: 600; color: #1D1D1F; margin-bottom: 12rpx; }
.row { display: flex; justify-content: space-between; gap: 24rpx; padding: 22rpx 0; border-bottom: 1rpx solid #F2F2F7; font-size: 28rpx; color: #86868B; }
.val { color: #1D1D1F; text-align: right; flex: 1; }
.price-inline { font-weight: 600; }
.actions { display: flex; flex-direction: column; gap: 16rpx; margin-top: 28rpx; }
.bank { margin-top: 20rpx; background: #F2F2F7; border-radius: 20rpx; padding: 8rpx 24rpx 20rpx; }
.bank-line { display: flex; justify-content: space-between; gap: 24rpx; padding: 16rpx 0; font-size: 26rpx; color: #86868B; }
.bank-line text:last-child { color: #1D1D1F; text-align: right; }
.bank-tip { margin-top: 8rpx; color: #86868B; font-size: 24rpx; line-height: 1.5; }
.empty { display: flex; align-items: center; justify-content: center; color: #86868B; }
</style>
