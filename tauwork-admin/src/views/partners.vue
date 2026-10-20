<template>
  <div class="panel">
    <div class="toolbar">
      <b>合伙人列表</b>
      <span class="grow" />
      <el-button type="primary" @click="openCreate">新增合伙人</el-button>
    </div>
    <el-table :data="rows" v-loading="loading">
      <el-table-column prop="name" label="姓名" width="110" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column label="上级" width="110">
        <template #default="{ row }">{{ row.parentName || '—' }}</template>
      </el-table-column>
      <el-table-column label="客户" width="120">
        <template #default="{ row }">直接 {{ row.directCount || 0 }} / 间接 {{ row.indirectCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="总佣金" width="110">
        <template #default="{ row }">¥ {{ money(row.commissionAmount) }}</template>
      </el-table-column>
      <el-table-column label="已提" width="100">
        <template #default="{ row }">¥ {{ money(row.withdrawnAmount) }}</template>
      </el-table-column>
      <el-table-column label="余额" width="100">
        <template #default="{ row }">¥ {{ money(row.balanceAmount) }}</template>
      </el-table-column>
      <el-table-column prop="dealCount" label="成交" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">明细</el-button>
          <el-button link @click="toggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="createOpen" title="新增合伙人" width="440px">
      <el-form :model="form" label-width="72px">
        <el-form-item label="姓名" required><el-input v-model="form.name" maxlength="32" /></el-form-item>
        <el-form-item label="电话" required><el-input v-model="form.phone" maxlength="11" placeholder="已有客户会直接开通" /></el-form-item>
        <el-form-item label="上级">
          <el-select v-model="form.parentId" clearable filterable placeholder="可选，下级客户计为间接客户" style="width:100%">
            <el-option v-for="item in partners" :key="item.id" :label="`${item.name || '未命名'} ${item.phone || ''}`" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createOpen = false">取消</el-button>
        <el-button type="primary" @click="create">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawer" :title="(detail.partner?.name || '') + ' · 合伙人明细'" size="760px">
      <template v-if="detail.partner">
        <p class="share">客户链接 /pages/index/index?partner={{ detail.partner.code }}</p>
        <p class="share">邀请合伙人 /pages/me/partner-join?partner={{ detail.partner.code }}</p>
        <div class="stats">
          <div class="stat"><b>{{ detail.partner.directCount || 0 }}</b><span>直接客户</span></div>
          <div class="stat"><b>{{ detail.partner.indirectCount || 0 }}</b><span>间接客户</span></div>
          <div class="stat"><b>¥ {{ money(detail.partner.commissionAmount) }}</b><span>总佣金</span></div>
          <div class="stat"><b>¥ {{ money(detail.partner.withdrawnAmount) }}</b><span>已提佣金</span></div>
          <div class="stat"><b>¥ {{ money(detail.partner.balanceAmount) }}</b><span>佣金余额</span></div>
        </div>
        <div class="stats slim">
          <div class="stat"><span>直接</span><b>¥ {{ money(detail.partner.directAmount) }}</b></div>
          <div class="stat"><span>间接</span><b>¥ {{ money(detail.partner.indirectAmount) }}</b></div>
          <div class="stat"><span>其它</span><b>¥ {{ money(detail.partner.otherAmount) }}</b></div>
        </div>
        <div class="bar">
          <el-button type="primary" @click="dealOpen = true">登记成交</el-button>
          <el-button @click="bindOpen = true">指定客户</el-button>
          <el-button @click="otherOpen = true">其它佣金</el-button>
          <el-button @click="withdrawOpen = true">登记提现</el-button>
        </div>
        <h4>客户</h4>
        <el-table :data="detail.customers" size="small">
          <el-table-column label="关系" width="80">
            <template #default="{ row }">{{ row.relation === 'INDIRECT' ? '间接' : '直接' }}</template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="phone" label="电话" width="140" />
          <el-table-column label="来源" width="110">
            <template #default="{ row }">{{ row.partnerBindSource === 'LINK' ? '分享链接' : '后台指定' }}</template>
          </el-table-column>
          <el-table-column label="归属时间" min-width="160">
            <template #default="{ row }">{{ time(row.partnerBindAt) }}</template>
          </el-table-column>
        </el-table>
        <h4>提成明细</h4>
        <el-table :data="detail.deals" size="small">
          <el-table-column label="客户" width="140">
            <template #default="{ row }">{{ row.customerName }}<div class="sub">{{ row.customerPhone }}</div></template>
          </el-table-column>
          <el-table-column label="层级" width="80">
            <template #default="{ row }">{{ levelMap[row.level] || '直接' }}</template>
          </el-table-column>
          <el-table-column prop="productName" label="项目" min-width="140" />
          <el-table-column prop="categoryName" label="类目" width="110" />
          <el-table-column label="成交额" width="100">
            <template #default="{ row }">¥ {{ money(row.orderAmount) }}</template>
          </el-table-column>
          <el-table-column label="比例" width="70">
            <template #default="{ row }">{{ row.rate }}%</template>
          </el-table-column>
          <el-table-column label="提成" width="100">
            <template #default="{ row }">¥ {{ money(row.commissionAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">{{ row.status === 'ACCRUED' ? '计入' : '已冲回' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button v-if="row.status === 'ACCRUED'" link type="danger" @click="reverse(row)">冲回</el-button>
            </template>
          </el-table-column>
        </el-table>
        <h4>提现</h4>
        <el-table :data="detail.withdraws || []" size="small">
          <el-table-column label="金额" width="120">
            <template #default="{ row }">¥ {{ money(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ withdrawMap[row.status] || row.status }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" />
          <el-table-column label="时间" min-width="150">
            <template #default="{ row }">{{ time(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button link type="primary" @click="reviewWithdraw(row, 'PAID')">通过</el-button>
                <el-button link type="danger" @click="reviewWithdraw(row, 'REJECTED')">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>

    <el-dialog v-model="bindOpen" title="指定客户" width="420px">
      <el-input v-model="bindPhone" maxlength="11" placeholder="客户手机号" />
      <template #footer>
        <el-button @click="bindOpen = false">取消</el-button>
        <el-button type="primary" @click="bindCustomer">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dealOpen" title="登记成交" width="480px">
      <el-form :model="deal" label-width="88px">
        <el-form-item label="客户" required>
          <el-select v-model="deal.customerId" filterable style="width:100%">
            <el-option v-for="item in directCustomers" :key="item.id" :label="`${item.name || '未命名'} ${item.phone || ''}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目" required>
          <el-select v-model="deal.productId" filterable style="width:100%">
            <el-option v-for="item in products" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="成交金额" required>
          <el-input v-model="deal.amount" placeholder="按这个金额乘类目比例" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dealOpen = false">取消</el-button>
        <el-button type="primary" @click="saveDeal">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="otherOpen" title="其它佣金" width="420px">
      <el-form label-width="72px">
        <el-form-item label="金额"><el-input v-model="other.amount" placeholder="直接计入总佣金" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="other.remark" maxlength="40" placeholder="例如活动奖励" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="otherOpen = false">取消</el-button>
        <el-button type="primary" @click="saveOther">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="withdrawOpen" title="登记提现" width="420px">
      <el-form label-width="72px">
        <el-form-item label="金额"><el-input v-model="cash.amount" placeholder="记为已提现" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="cash.remark" maxlength="40" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="withdrawOpen = false">取消</el-button>
        <el-button type="primary" @click="saveWithdraw">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'

const rows = ref([])
const loading = ref(false)
const createOpen = ref(false)
const form = reactive({ name: '', phone: '', parentId: null })
const partners = ref([])
const drawer = ref(false)
const detail = ref({ partner: null, customers: [], deals: [], withdraws: [] })
const levelMap = { DIRECT: '直接', INDIRECT: '间接', OTHER: '其它' }
const withdrawMap = { PENDING: '审核中', PAID: '已提', REJECTED: '驳回' }
const directCustomers = computed(() => (detail.value.customers || []).filter(item => item.relation !== 'INDIRECT'))
const otherOpen = ref(false)
const other = reactive({ amount: '', remark: '' })
const withdrawOpen = ref(false)
const cash = reactive({ amount: '', remark: '' })
const bindOpen = ref(false)
const bindPhone = ref('')
const dealOpen = ref(false)
const deal = reactive({ customerId: null, productId: null, amount: '' })
const products = ref([])

function money(v) { return Number(v || 0).toFixed(2) }
function time(v) { return v ? String(v).replace('T', ' ').slice(0, 16) : '—' }

async function load() {
  loading.value = true
  try { rows.value = await http.get('/admin/partner/list') } finally { loading.value = false }
}

async function openCreate() {
  form.name = ''
  form.phone = ''
  form.parentId = null
  partners.value = await http.get('/admin/partner/options')
  createOpen.value = true
}

async function create() {
  if (!form.name.trim() || !/^1\d{10}$/.test(form.phone)) return ElMessage.warning('请填写姓名和 11 位手机号')
  await http.post('/admin/partner', { name: form.name.trim(), phone: form.phone, parentId: form.parentId })
  ElMessage.success('合伙人已开通')
  createOpen.value = false
  load()
}

async function toggle(row) {
  await http.put(`/admin/partner/${row.id}/status/${row.status === 1 ? 0 : 1}`)
  load()
}

async function openDetail(row) {
  detail.value = await http.get(`/admin/partner/${row.id}`)
  if (!products.value.length) {
    const page = await http.get('/admin/product/page', { params: { page: 1, size: 200 } })
    products.value = page.records || []
  }
  drawer.value = true
}

async function bindCustomer() {
  if (!/^1\d{10}$/.test(bindPhone.value)) return ElMessage.warning('请输入 11 位手机号')
  await http.post(`/admin/partner/${detail.value.partner.id}/customer`, { phone: bindPhone.value })
  ElMessage.success('客户已归属')
  bindOpen.value = false
  bindPhone.value = ''
  openDetail(detail.value.partner)
  load()
}

async function saveDeal() {
  const amount = Number(deal.amount)
  if (!deal.customerId || !deal.productId || !(amount > 0)) return ElMessage.warning('请选择客户、项目，并填写成交金额')
  await http.post(`/admin/partner/${detail.value.partner.id}/deal`, { ...deal, amount })
  ElMessage.success('成交已计入提成')
  dealOpen.value = false
  deal.customerId = null
  deal.productId = null
  deal.amount = ''
  openDetail(detail.value.partner)
  load()
}

async function saveOther() {
  const amount = Number(other.amount)
  if (!(amount > 0)) return ElMessage.warning('请填写金额')
  await http.post(`/admin/partner/${detail.value.partner.id}/other`, { amount, remark: other.remark })
  ElMessage.success('其它佣金已计入')
  otherOpen.value = false
  other.amount = ''
  other.remark = ''
  openDetail(detail.value.partner)
  load()
}

async function saveWithdraw() {
  const amount = Number(cash.amount)
  if (!(amount > 0)) return ElMessage.warning('请填写金额')
  await http.post(`/admin/partner/${detail.value.partner.id}/withdraw`, { amount, remark: cash.remark })
  ElMessage.success('提现已登记')
  withdrawOpen.value = false
  cash.amount = ''
  cash.remark = ''
  openDetail(detail.value.partner)
  load()
}

async function reviewWithdraw(row, status) {
  await http.put(`/admin/partner/withdraw/${row.id}/${status}`)
  openDetail(detail.value.partner)
  load()
}

async function reverse(row) {
  await ElMessageBox.confirm('冲回后这笔提成不再计入金额和成交单数', '冲回提成')
  await http.put(`/admin/partner/deal/${row.id}/reverse`)
  openDetail(detail.value.partner)
  load()
}

onMounted(load)
</script>

<style scoped>
.grow { flex: 1; }
.share { color: var(--abos-muted); font-size: 13px; }
.bar { display: flex; gap: 8px; margin: 8px 0 4px; }
h4 { margin: 16px 0 8px; }
.sub { color: var(--abos-muted); font-size: 12px; }
.stats { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; margin: 12px 0; }
.stats.slim { grid-template-columns: repeat(3, 1fr); }
.stat { background: #f6f1ea; border-radius: 8px; padding: 10px 12px; }
.stat b { display: block; font-size: 16px; }
.stat span { color: var(--abos-muted); font-size: 12px; }
</style>
