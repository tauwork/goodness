<template>
  <view class="page" v-if="row">
    <view class="card">
      <view class="title">{{ row.displayName || '公司注册' }}</view>
      <view class="sub">{{ row.productName }} · {{ row.orderNo }}</view>
      <view class="steps">
        <view class="step" v-for="s in row.steps" :key="s.code">
          <view class="dot" :class="s.state"></view>
          <text class="sl" :class="s.state">{{ s.label }}</text>
        </view>
      </view>
    </view>

    <view class="card" v-if="row.caseStatus === 'CANCELED'">订单已取消，工单已停止。</view>
    <view class="card" v-else-if="row.caseStatus === 'DONE'">注册证书和商业登记证已领取，公司注册已完成。</view>

    <view class="card" v-else-if="row.step === 'NAME'">
      <view class="h">公司核名</view>
      <view class="tip">填写拟注册的公司名称。运营先人工核对能否使用，后续会改为接口自动查册。</view>
      <view class="warn" v-if="row.stepStatus === 'REJECTED' && row.remark">{{ row.remark }}</view>
      <view class="wait" v-else-if="row.stepStatus === 'WAIT_REVIEW'">名称已提交，等待运营核对。</view>
      <block v-if="canEdit">
        <input class="inp" v-model="nameForm.companyName" maxlength="64" placeholder="中文名称，例如九千善有限公司" />
        <input class="inp" v-model="nameForm.companyNameEn" maxlength="128" placeholder="英文名称，例如 GOODNESS LIMITED" />
        <view class="btn" @tap="submitName">提交核名</view>
      </block>
      <view v-else class="readonly">
        <view>中文：{{ row.companyName || '未填' }}</view>
        <view>英文：{{ row.companyNameEn || '未填' }}</view>
      </view>
    </view>

    <view class="card" v-else-if="row.step === 'PROFILE'">
      <view class="h">公司资料</view>
      <view class="tip">填写主营业务、注册资金，以及股东和董事。持股比例合计须为 100%。注册资金默认 1 万港币。</view>
      <view class="warn" v-if="row.stepStatus === 'REJECTED' && row.remark">{{ row.remark }}</view>
      <view class="wait" v-else-if="row.stepStatus === 'WAIT_REVIEW'">资料已提交，等待运营核对。</view>
      <block v-if="canEdit">
        <textarea class="inp ta" v-model="profile.bizScope" maxlength="500" placeholder="主营业务" />
        <view class="line">
          <text>注册资金</text>
          <input class="inp grow" v-model="profile.capitalAmount" type="digit" />
          <text>港币</text>
        </view>
        <view class="person" v-for="(m, i) in profile.members" :key="i">
          <view class="person-head">
            <text>成员 {{ i + 1 }}</text>
            <text class="link" v-if="profile.members.length > 1" @tap="profile.members.splice(i, 1)">删除</text>
          </view>
          <input class="inp" v-model="m.name" maxlength="32" placeholder="姓名" />
          <view class="roles">
            <text v-for="r in roles" :key="r.v" class="role" :class="{ on: m.role === r.v }" @tap="m.role = r.v">{{ r.label }}</text>
          </view>
          <input v-if="m.role !== 'DIRECTOR'" class="inp" v-model="m.shareRatio" type="text" placeholder="持股比例 %" />
        </view>
        <view class="link add" @tap="addMember">添加股东或董事</view>
        <view class="btn" @tap="submitProfile">提交资料</view>
      </block>
      <view v-else>
        <view class="readonly">主营业务：{{ row.bizScope }}</view>
        <view class="readonly">注册资金：{{ row.capitalAmount }} 港币</view>
        <view class="readonly" v-for="m in row.members" :key="m.id">{{ m.name }} · {{ roleText(m.role) }}<text v-if="m.role !== 'DIRECTOR'"> · {{ m.shareRatio }}%</text></view>
      </view>
    </view>

    <view class="card" v-else-if="row.step === 'KYC'">
      <view class="h">身份资料</view>
      <view class="tip">每位股东和董事都需要身份证正面、反面，以及手持身份证照片。</view>
      <view class="warn" v-if="row.stepStatus === 'REJECTED' && row.remark">{{ row.remark }}</view>
      <view class="wait" v-else-if="row.stepStatus === 'WAIT_REVIEW'">资料已提交，等待运营核对。</view>
      <view class="person" v-for="m in kyc" :key="m.id">
        <view class="person-head"><text>{{ m.name }} · {{ roleText(m.role) }}</text></view>
        <view class="shots">
          <view class="shot" @tap="canEdit && pick(m, 'idFrontUrl')">
            <image v-if="m.idFrontUrl" :src="photo(m.idFrontUrl)" mode="aspectFill" />
            <text v-else>身份证正面</text>
          </view>
          <view class="shot" @tap="canEdit && pick(m, 'idBackUrl')">
            <image v-if="m.idBackUrl" :src="photo(m.idBackUrl)" mode="aspectFill" />
            <text v-else>身份证反面</text>
          </view>
          <view class="shot" @tap="canEdit && pick(m, 'idHoldUrl')">
            <image v-if="m.idHoldUrl" :src="photo(m.idHoldUrl)" mode="aspectFill" />
            <text v-else>手持身份证</text>
          </view>
        </view>
      </view>
      <view class="btn" v-if="canEdit" @tap="submitKyc">提交身份资料</view>
    </view>

    <view class="card" v-else-if="row.step === 'SIGN'">
      <view class="h">签署文件</view>
      <view class="tip">香港公司注册需签署法团成立表格（NNC1）和组织章程大纲。运营备妥文件后，请在这里确认签署。</view>
      <view class="wait" v-if="row.stepStatus === 'WAIT_OPS'">运营正在准备注册文件。</view>
      <block v-else-if="row.stepStatus === 'WAIT_USER'">
        <view class="readonly">{{ row.remark || '注册文件已备妥，请确认签署。' }}</view>
        <view class="btn" @tap="confirmSign">确认签署</view>
      </block>
    </view>

    <view class="card" v-else-if="row.step === 'FILING'">
      <view class="h">递交注册处</view>
      <view class="wait">资料已齐，运营正在向香港公司注册处递交。</view>
    </view>

    <view class="card" v-else-if="row.step === 'CERT'">
      <view class="h">领取证书</view>
      <view class="wait">已递交注册处，等待领取公司注册证书和商业登记证。</view>
    </view>
  </view>
</template>

<script>
import api, { BASE, ensureLogin } from '../../utils/request'
import { mediaUrl } from '../../utils/visual'

export default {
  data() {
    return {
      id: '',
      row: null,
      nameForm: { companyName: '', companyNameEn: '' },
      profile: { bizScope: '', capitalAmount: '10000', members: [] },
      kyc: [],
      roles: [
        { v: 'SHAREHOLDER', label: '股东' },
        { v: 'DIRECTOR', label: '董事' },
        { v: 'BOTH', label: '股东兼董事' }
      ]
    }
  },
  computed: {
    canEdit() {
      return this.row && (this.row.stepStatus === 'WAIT_USER' || this.row.stepStatus === 'REJECTED')
    }
  },
  onLoad(q) {
    this.id = q.id
    this.load()
  },
  methods: {
    load() {
      api.get(`/api/mini/co-reg/${this.id}`, null, true).then(row => {
        this.row = row
        this.nameForm.companyName = row.companyName || ''
        this.nameForm.companyNameEn = row.companyNameEn || ''
        this.profile.bizScope = row.bizScope || ''
        this.profile.capitalAmount = row.capitalAmount != null ? String(row.capitalAmount) : '10000'
        const members = row.members || []
        this.profile.members = members.length
          ? members.map(m => ({ name: m.name, role: m.role, shareRatio: m.role === 'DIRECTOR' ? '' : String(m.shareRatio) }))
          : [{ name: '', role: 'BOTH', shareRatio: '100' }]
        this.kyc = members.map(m => ({
          id: m.id,
          name: m.name,
          role: m.role,
          idFrontUrl: m.idFrontUrl || '',
          idBackUrl: m.idBackUrl || '',
          idHoldUrl: m.idHoldUrl || ''
        }))
      })
    },
    roleText(role) {
      return { SHAREHOLDER: '股东', DIRECTOR: '董事', BOTH: '股东兼董事' }[role] || role
    },
    photo(url) {
      return mediaUrl(url, BASE)
    },
    addMember() {
      this.profile.members.push({ name: '', role: 'SHAREHOLDER', shareRatio: '' })
    },
    submitName() {
      api.put(`/api/mini/co-reg/${this.id}/name`, this.nameForm, true).then(() => {
        uni.showToast({ title: '已提交', icon: 'none' })
        this.load()
      })
    },
    submitProfile() {
      const members = this.profile.members.map(m => ({
        name: (m.name || '').trim(),
        role: m.role,
        shareRatio: m.role === 'DIRECTOR' ? 0 : Number(m.shareRatio)
      }))
      api.put(`/api/mini/co-reg/${this.id}/profile`, {
        bizScope: this.profile.bizScope,
        capitalAmount: Number(this.profile.capitalAmount || 10000),
        members
      }, true).then(() => {
        uni.showToast({ title: '已提交', icon: 'none' })
        this.load()
      })
    },
    submitKyc() {
      api.put(`/api/mini/co-reg/${this.id}/kyc`, {
        members: this.kyc.map(m => ({
          id: m.id,
          idFrontUrl: m.idFrontUrl,
          idBackUrl: m.idBackUrl,
          idHoldUrl: m.idHoldUrl
        }))
      }, true).then(() => {
        uni.showToast({ title: '已提交', icon: 'none' })
        this.load()
      })
    },
    confirmSign() {
      api.post(`/api/mini/co-reg/${this.id}/sign`, {}, true).then(() => {
        uni.showToast({ title: '已确认', icon: 'none' })
        this.load()
      })
    },
    async pick(member, field) {
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
              member[field] = body.data.url
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
.page { min-height: 100vh; background: #F2F2F7; padding: 16rpx 16rpx 48rpx; }
.card { background: #fff; border-radius: 24rpx; padding: 28rpx; margin-bottom: 16rpx; }
.title { color: #1D1D1F; font-size: 34rpx; font-weight: 600; }
.sub, .tip, .readonly, .wait { margin-top: 12rpx; color: #86868B; font-size: 26rpx; line-height: 1.6; }
.h { color: #1D1D1F; font-size: 30rpx; font-weight: 600; }
.steps { display: flex; flex-wrap: wrap; gap: 16rpx 20rpx; margin-top: 24rpx; }
.step { display: flex; align-items: center; gap: 8rpx; }
.dot { width: 14rpx; height: 14rpx; border-radius: 50%; background: #D1D1D6; }
.dot.done { background: #1D1D1F; }
.dot.current { background: #C4A35A; }
.sl { font-size: 22rpx; color: #86868B; }
.sl.current, .sl.done { color: #1D1D1F; }
.warn { margin-top: 16rpx; color: #C4151C; font-size: 26rpx; line-height: 1.5; }
.inp { margin-top: 16rpx; background: #F2F2F7; border-radius: 16rpx; padding: 20rpx 24rpx; font-size: 28rpx; color: #1D1D1F; }
.ta { width: 100%; height: 160rpx; box-sizing: border-box; }
.line { display: flex; align-items: center; gap: 16rpx; margin-top: 8rpx; color: #1D1D1F; font-size: 28rpx; }
.grow { flex: 1; }
.person { margin-top: 20rpx; padding-top: 8rpx; }
.person-head { display: flex; justify-content: space-between; color: #1D1D1F; font-size: 26rpx; font-weight: 500; }
.roles { display: flex; gap: 12rpx; margin-top: 16rpx; }
.role { padding: 10rpx 18rpx; border-radius: 999rpx; background: #F2F2F7; color: #3A3A3C; font-size: 24rpx; }
.role.on { background: #1D1D1F; color: #fff; }
.link { color: #007AFF; font-size: 24rpx; font-weight: 400; }
.add { display: inline-block; margin-top: 16rpx; }
.shots { display: flex; gap: 12rpx; margin-top: 16rpx; }
.shot { width: 200rpx; height: 200rpx; border-radius: 16rpx; background: #F2F2F7; overflow: hidden; display: flex; align-items: center; justify-content: center; color: #86868B; font-size: 22rpx; text-align: center; padding: 12rpx; box-sizing: border-box; }
.shot image { width: 100%; height: 100%; }
.btn { margin-top: 28rpx; height: 88rpx; line-height: 88rpx; text-align: center; border-radius: 999rpx; background: #1D1D1F; color: #fff; font-size: 30rpx; font-weight: 500; }
</style>
