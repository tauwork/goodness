<template>
  <view class="page">
    <view class="intro">
      <image class="intro-img" src="/static/visual/living.jpg" mode="aspectFill" />
      <view class="intro-copy">
        <text class="kicker">FAMILY</text>
        <view class="intro-title">家庭档案</view>
        <view class="intro-desc">写下来，评估和顾问沟通会更准确。</view>
      </view>
    </view>

    <view class="card mem" v-for="m in list" :key="m.id">
      <view class="mhead">
        <text class="mtag">{{ relText(m.relation) }}</text>
        <text class="mname">{{ m.name || '未命名' }}</text>
        <text class="del" @tap="del(m)">删除</text>
      </view>
      <view class="sub">{{ m.schoolStage || '未填写阶段' }}<text v-if="m.birthDate">  ·  {{ m.birthDate }}</text></view>
    </view>
    <view v-if="!list.length" class="empty-box">
      <image src="/static/visual/education.jpg" mode="aspectFill" />
      <text>还没有家庭成员</text>
    </view>

    <view class="card">
      <view class="kicker">ADD</view>
      <view class="ft">添加成员</view>
      <view class="chips">
        <view v-for="r in ['SELF','SPOUSE','CHILD']" :key="r" class="chip" :class="{ on: form.relation === r }"
              @tap="form.relation = r">{{ relText(r) }}</view>
      </view>
      <input class="inp" v-model="form.name" placeholder="姓名或称呼" />
      <input class="inp" v-model="form.schoolStage" placeholder="在读阶段，孩子选填" />
      <picker mode="date" :value="form.birthDate" @change="e => form.birthDate = e.detail.value">
        <view class="inp picker">{{ form.birthDate || '出生日期，选填' }}</view>
      </picker>
      <view class="btn-main" @tap="add">保存成员</view>
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
      const { confirm } = await new Promise(r => uni.showModal({ title: '确认删除', content: `删除「${m.name || '该成员'}」？`, success: r }))
      if (!confirm) return
      await api.delete(`/api/mini/family/${m.id}`)
      this.load()
    }
  }
}
</script>

<style scoped>
.intro { position: relative; height: 280rpx; margin: 20rpx 28rpx 8rpx; border-radius: 28rpx; overflow: hidden; }
.intro-img { width: 100%; height: 100%; }
.intro-copy { position: absolute; left: 0; right: 0; bottom: 0; padding: 24rpx 28rpx; color: #fff; background: linear-gradient(transparent, rgba(16,14,12,.7)); }
.intro-title { font-size: 40rpx; font-weight: 500; margin-top: 6rpx; }
.intro-desc { font-size: 22rpx; color: rgba(255,255,255,.82); margin-top: 6rpx; }
.kicker { color: #E7D3AE; }
.card .kicker { color: #9A7B4F; }
.mem { padding: 28rpx; }
.mhead { display: flex; align-items: center; gap: 16rpx; }
.mtag { font-size: 20rpx; color: #6F6252; background: #F3EEE6; border-radius: 999rpx; padding: 4rpx 16rpx; }
.mname { flex: 1; font-size: 30rpx; font-weight: 500; }
.del { color: #8C4A42; font-size: 22rpx; }
.ft { font-size: 32rpx; margin: 8rpx 0 20rpx; }
.chips { display: flex; gap: 14rpx; }
.chip { background: #F3EEE6; border-radius: 999rpx; padding: 12rpx 28rpx; font-size: 24rpx; color: #6F6252; }
.chip.on { background: #1C1915; color: #F6E7C8; }
.inp { background: #F3EEE6; border-radius: 16rpx; padding: 22rpx 24rpx; font-size: 26rpx; margin-top: 16rpx; }
.btn-main { margin-top: 28rpx; }
</style>
