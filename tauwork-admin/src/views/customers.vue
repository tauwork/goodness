<template>
  <el-card shadow="never">
    <template #header>
      <div class="bar">
        <b>客户库</b>
        <el-radio-group v-model="stage" @change="load">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button v-for="(label, key) in stageMap" :key="key" :value="key">{{ label }}</el-radio-button>
        </el-radio-group>
        <el-input v-model="keyword" placeholder="姓名、手机号、微信" clearable style="width:200px" @keyup.enter="load" @clear="load" />
        <el-button @click="load">搜索</el-button>
        <el-button type="primary" style="margin-left:auto" @click="openCreate">新建客户</el-button>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column label="姓名" width="120">
        <template #default="{ row }">{{ row.name || '未命名' }}</template>
      </el-table-column>
      <el-table-column label="手机号" width="130">
        <template #default="{ row }">{{ row.phone || '未绑定' }}</template>
      </el-table-column>
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column label="阶段" width="100">
        <template #default="{ row }">
          <el-tag :type="stageType[row.stage]" size="small">{{ stageMap[row.stage] || row.stage }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="来源" width="110">
        <template #default="{ row }">{{ sourceMap[row.source] || row.source || '—' }}</template>
      </el-table-column>
      <el-table-column label="顾问" width="110">
        <template #default="{ row }">{{ row.ownerName || '—' }}</template>
      </el-table-column>
      <el-table-column label="下次跟进" width="150">
        <template #default="{ row }">{{ time(row.nextFollowAt) }}</template>
      </el-table-column>
      <el-table-column label="注册时间" width="150">
        <template #default="{ row }">{{ time(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">档案</el-button>
          <el-button v-if="row.stage === 'POOL'" size="small" type="primary" plain @click="claim(row)">领取</el-button>
          <el-button v-if="row.stage !== 'POOL' && row.stage !== 'BLACKLIST'" size="small" plain @click="release(row)">退回公海</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-drawer v-model="drawer" :title="(detail.customer?.name || '') + ' · 客户档案'" size="720px">
    <template v-if="detail.customer">
      <el-alert v-if="detail.customer.stage === 'BLACKLIST'" title="该客户在黑名单中，不再安排跟进和预约" type="error" show-icon :closable="false" style="margin-bottom:12px" />
      <div class="bar" style="margin-bottom:12px">
        <el-button v-for="(label, key) in stageMap" :key="key" size="small"
                   :type="detail.customer.stage === key ? 'primary' : 'default'" @click="setStage(key)">{{ label }}</el-button>
      </div>
      <el-form :model="form" label-width="88px">
        <el-form-item label="姓名" required><el-input v-model="form.name" maxlength="32" /></el-form-item>
        <el-form-item label="手机号" required><el-input v-model="form.phone" maxlength="11" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="M">男</el-radio>
            <el-radio value="F">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="城市"><el-input v-model="form.city" maxlength="32" /></el-form-item>
        <el-form-item label="微信"><el-input v-model="form.wechat" maxlength="64" /></el-form-item>
        <el-form-item label="公司或职业"><el-input v-model="form.company" maxlength="64" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" maxlength="120" /></el-form-item>
        <el-form-item label="家庭情况"><el-input v-model="form.familyNote" type="textarea" :rows="2" maxlength="500" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="1000" /></el-form-item>
        <el-form-item v-if="canPartner" label="归属合伙人">
          <el-select v-model="partnerId" clearable filterable placeholder="未归属" style="width:280px">
            <el-option v-for="item in partners" :key="item.id" :label="`${item.name} ${item.phone || ''}`" :value="item.id" />
          </el-select>
          <el-button style="margin-left:8px" @click="savePartner">保存归属</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存档案</el-button>
          <el-button @click="followOpen = true">写跟进</el-button>
        </el-form-item>
      </el-form>

      <h4>家庭成员</h4>
      <el-table :data="detail.family" size="small">
        <el-table-column label="关系" width="80">
          <template #default="{ row }">{{ relationMap[row.relation] || row.relation }}</template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="birthDate" label="生日" width="120" />
        <el-table-column prop="schoolStage" label="在读阶段" />
        <el-table-column label="" width="70">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeFamily(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="bar" style="margin:8px 0 16px">
        <el-select v-model="family.relation" style="width:100px">
          <el-option label="本人" value="SELF" />
          <el-option label="配偶" value="SPOUSE" />
          <el-option label="子女" value="CHILD" />
        </el-select>
        <el-input v-model="family.name" placeholder="姓名" style="width:120px" />
        <el-date-picker v-model="family.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="生日" />
        <el-input v-model="family.schoolStage" placeholder="在读阶段" style="width:140px" />
        <el-button @click="addFamily">添加</el-button>
      </div>

      <h4>最近跟进</h4>
      <el-empty v-if="!detail.follows.length" description="还没有跟进" :image-size="64" />
      <div v-for="item in detail.follows" :key="item.id" class="log">
        <b>{{ followMap[item.followType] || item.followType }}</b>
        <span>{{ time(item.followAt) }}</span>
        <p>{{ item.content }}</p>
      </div>

      <h4>最近预约</h4>
      <el-empty v-if="!detail.appointments.length" description="还没有预约" :image-size="64" />
      <div v-for="item in detail.appointments" :key="item.id" class="log">
        <b>{{ modeMap[item.mode] || item.mode }} · {{ item.productName || item.topic }}</b>
        <span>{{ item.bookDate }} {{ item.timeSlot }} · {{ bookStatus[item.status] || item.status }}</span>
      </div>
    </template>
  </el-drawer>

  <el-dialog v-model="createOpen" title="新建客户" width="480px">
    <el-form :model="createForm" label-width="88px">
      <el-form-item label="姓名" required><el-input v-model="createForm.name" /></el-form-item>
      <el-form-item label="手机号" required><el-input v-model="createForm.phone" maxlength="11" /></el-form-item>
      <el-form-item label="城市"><el-input v-model="createForm.city" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createOpen = false">取消</el-button>
      <el-button type="primary" @click="createCustomer">保存到公海</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="followOpen" title="写跟进" width="520px">
    <el-form :model="follow" label-width="88px">
      <el-form-item label="方式">
        <el-select v-model="follow.followType" style="width:160px">
          <el-option v-for="(label, key) in followMap" :key="key" :label="label" :value="key" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容" required>
        <el-input v-model="follow.content" type="textarea" :rows="4" maxlength="2000" placeholder="拜访、会议或沟通里发生了什么" />
      </el-form-item>
      <el-form-item label="下次跟进">
        <el-date-picker v-model="follow.nextFollowAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="followOpen = false">取消</el-button>
      <el-button type="primary" @click="saveFollow">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const stageMap = { POOL: '公海客户', INTENT: '意向客户', SIGNED: '签约客户', REPEAT: '复购客户', BLACKLIST: '黑名单' }
const stageType = { POOL: 'info', INTENT: 'warning', SIGNED: 'success', REPEAT: 'success', BLACKLIST: 'danger' }
const sourceMap = { ASSESS: '智能评估', BOOKING: '预约登记', CONTENT: '内容留资', REFERRAL: '转介绍', EVENT: '活动', STAFF: '后台建档', OTHER: '小程序' }
const relationMap = { SELF: '本人', SPOUSE: '配偶', CHILD: '子女' }
const followMap = { VISIT: '拜访', MEETING: '会议', CALL: '电话', WECHAT: '微信', OTHER: '其他' }
const modeMap = { VIDEO: '视频咨询', FUTIAN: '深圳·福田', BAOAN: '深圳·宝安', KWAI_TSING: '香港·葵青' }
const bookStatus = { PENDING: '待确认', CONFIRMED: '已确认', DONE: '已完成', CANCELED: '已取消', NO_SHOW: '爽约' }

const list = ref([])
const stage = ref('')
const keyword = ref('')
const drawer = ref(false)
const detail = ref({ customer: null, family: [], follows: [], appointments: [], leads: [] })
const form = reactive(blankProfile())
const createOpen = ref(false)
const createForm = reactive({ name: '', phone: '', city: '' })
const me = JSON.parse(localStorage.getItem('user') || '{}')
const canPartner = (me.perms || []).includes('*') || (me.perms || []).includes('partner:manage')
const partners = ref([])
const partnerId = ref(null)
const followOpen = ref(false)
const follow = reactive({ followType: 'VISIT', content: '', nextFollowAt: null })
const family = reactive({ relation: 'CHILD', name: '', birthDate: null, schoolStage: '' })

function blankProfile() {
  return { name: '', phone: '', gender: '', city: '', wechat: '', company: '', address: '', familyNote: '', remark: '' }
}
function time(v) { return v ? String(v).replace('T', ' ').slice(0, 16) : '—' }

async function load() {
  try {
    const page = await http.get('/admin/customer/page', {
      params: { page: 1, size: 50, stage: stage.value || undefined, keyword: keyword.value || undefined }
    })
    list.value = page.records
  } catch (e) { ElMessage.error(e.message) }
}

async function openDetail(row) {
  detail.value = await http.get(`/admin/customer/${row.id}`)
  Object.assign(form, blankProfile(), detail.value.customer)
  partnerId.value = detail.value.customer.partnerId || null
  if (canPartner && !partners.value.length) partners.value = await http.get('/admin/partner/options')
  drawer.value = true
}

async function savePartner() {
  await http.put(`/admin/partner/customer/${detail.value.customer.id}`, { partnerId: partnerId.value || null })
  ElMessage.success(partnerId.value ? '已指定合伙人' : '已取消归属')
  await openDetail(detail.value.customer)
}

function openCreate() {
  Object.assign(createForm, { name: '', phone: '', city: '' })
  createOpen.value = true
}

async function createCustomer() {
  try {
    await http.post('/admin/customer', { ...createForm, source: 'STAFF' })
    ElMessage.success('已放入公海')
    createOpen.value = false
    stage.value = 'POOL'
    load()
  } catch (e) { ElMessage.error(e.message) }
}

async function saveProfile() {
  try {
    await http.put(`/admin/customer/${detail.value.customer.id}`, form)
    ElMessage.success('档案已保存')
    await openDetail(detail.value.customer)
    load()
  } catch (e) { ElMessage.error(e.message) }
}

async function setStage(key) {
  if (key === detail.value.customer.stage) return
  try {
    await http.put(`/admin/customer/${detail.value.customer.id}/stage/${key}`)
    ElMessage.success('阶段已更新')
    await openDetail(detail.value.customer)
    load()
  } catch (e) { ElMessage.error(e.message) }
}

async function claim(row) {
  await http.put(`/admin/customer/${row.id}/claim`)
  ElMessage.success('已领取为意向客户')
  load()
}

async function release(row) {
  await ElMessageBox.confirm(`把「${row.name}」退回公海？顾问会被清空。`, '退回公海')
  await http.put(`/admin/customer/${row.id}/release`)
  ElMessage.success('已退回公海')
  load()
}

async function addFamily() {
  try {
    await http.post(`/admin/customer/${detail.value.customer.id}/family`, { ...family })
    Object.assign(family, { relation: 'CHILD', name: '', birthDate: null, schoolStage: '' })
    await openDetail(detail.value.customer)
  } catch (e) { ElMessage.error(e.message) }
}

async function removeFamily(row) {
  await http.delete(`/admin/customer/${detail.value.customer.id}/family/${row.id}`)
  await openDetail(detail.value.customer)
}

async function saveFollow() {
  try {
    await http.post('/admin/follow', {
      customerId: detail.value.customer.id,
      followType: follow.followType,
      content: follow.content,
      nextFollowAt: follow.nextFollowAt
    })
    follow.content = ''
    follow.nextFollowAt = null
    followOpen.value = false
    ElMessage.success('跟进已记录')
    await openDetail(detail.value.customer)
    load()
  } catch (e) { ElMessage.error(e.message) }
}

onMounted(load)
</script>

<style scoped>
.bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
h4 { margin: 18px 0 8px; font-size: 14px; }
.log { padding: 8px 0; border-bottom: 1px solid #F0F2F6; }
.log span { margin-left: 8px; color: #6B7C93; font-size: 12px; }
.log p { margin: 4px 0 0; white-space: pre-wrap; }
</style>
