<template>
  <view class="page">
    <view class="card mem" v-for="m in list" :key="m.id">
      <view class="mhead">
        <text class="mtag">{{ relText(m.relation) }}</text>
        <text class="mname">{{ m.name || '未命名' }}</text>
        <text class="del" @tap="del(m)">删除</text>
      </view>
      <view class="sub">{{ m.schoolStage || '—' }} {{ m.birthDate || '' }}</view>
    </view>
    <view v-if="!list.length" class="card" style="text-align:center;color:#8A94A8">尚未添加家庭成员</view>

    <view class="card">
      <view class="ft">添加成员</view>
      <view class="chips">
        <view v-for="r in ['SELF','SPOUSE','CHILD']" :key="r" class="chip" :class="{ on: form.relation === r }"
              @tap="form.relation = r">{{ relText(r) }}</view>
      </view>
      <input class="inp" v-model="form.name" placeholder="姓名 / 称呼" style="margin-top:20rpx" />
      <input class="inp" v-model="form.schoolStage" placeholder="在读阶段（孩子，选填）" style="margin-top:16rpx" />
      <picker mode="date" :value="form.birthDate" @change="e => form.birthDate = e.detail.value">
        <view class="inp" style="margin-top:16rpx">{{ form.birthDate || '出生日期（选填）' }}</view>
      </picker>
      <view class="btn-main" style="margin-top:24rpx" @tap="add">保存成员</view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/request'

export default {
  data() {
    return { list: [], form: { relation: 'CHILD', name: '', schoolStage: '', birthDate: '' } }
  },
  onShow() { this.load() },
  methods: {
    relText(r) { return { SELF: '本人', SPOUSE: '配偶', CHILD: '孩子' }[r] || r },
    load() {
      api.get('/api/mini/family/list', null, true).then(d => this.list = d)
    },
    async add() {
      if (!this.form.name) return uni.showToast({ title: '请填写姓名', icon: 'none' })
      await api.post('/api/mini/family', this.form)
      uni.showToast({ title: '已添加', icon: 'success' })
      this.form = { relation: 'CHILD', name: '', schoolStage: '', birthDate: '' }
      this.load()
    },
    async del(m) {
      const { confirm } = await new Promise(r => uni.showModal({ title: '确认删除', content: `删除「${m.name || relText(m.relation)}」？`, success: r }))
      if (!confirm) return
      await api.delete(`/api/mini/family/${m.id}`)
      this.load()
    }
  }
}
</script>

<style scoped>
.mem { padding: 24rpx 28rpx; }
.mhead { display: flex; align-items: center; gap: 16rpx; }
.mtag { font-size: 20rpx; color: #16355E; background: #EDF2F9; border-radius: 8rpx; padding: 4rpx 14rpx; }
.mname { flex: 1; font-size: 30rpx; font-weight: 600; }
.del { color: #D9534F; font-size: 22rpx; }
.ft { font-size: 28rpx; font-weight: 600; margin-bottom: 18rpx; }
.chips { display: flex; gap: 14rpx; }
.chip { border: 1rpx solid #E8ECF3; background: #fff; border-radius: 28rpx; padding: 12rpx 32rpx; font-size: 24rpx; }
.chip.on { background: #16355E; color: #fff; border-color: #16355E; }
.inp { background: #F4F6FA; border-radius: 14rpx; padding: 20rpx 24rpx; font-size: 26rpx; }
</style>
