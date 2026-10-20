// 真机调试访问的是手机自己的 localhost，这里用电脑的局域网地址。换网络后改这一行。
export const BASE = 'http://192.168.1.232:8080'

function request(method, url, data, needAuth) {
  return new Promise((resolve, reject) => {
    if (needAuth && !uni.getStorageSync('token')) {
      return ensureLogin().then(() => request(method, url, data, needAuth)).then(resolve).catch(reject)
    }
    // 过滤值为 undefined/null 的参数，避免拼成 "key=undefined" 字符串打挂后端
    const clean = {}
    if (data) {
      for (const k of Object.keys(data)) {
        if (data[k] !== undefined && data[k] !== null) clean[k] = data[k]
      }
    }
    uni.request({
      url: BASE + url,
      method,
      data: clean,
      header: { Authorization: uni.getStorageSync('token') || '' },
      success: res => {
        const body = res.data
        if (body.code === 0) return resolve(body.data)
        if (body.code === 2001) {
          uni.removeStorageSync('token')
          return ensureLogin().then(() => request(method, url, data, needAuth)).then(resolve).catch(reject)
        }
        uni.showToast({ title: body.msg || '请求失败', icon: 'none' })
        reject(new Error(body.msg))
      },
      fail: err => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

/**
 * 登录：优先 uni.login 拿 code（微信环境）；失败（H5/工具无 appid）用固定 mock code 兜底，
 * 后端 appid 未配置时对任意 code 均走 mock 身份（mock-{code}，同一 code 同一客户）
 */
export function ensureLogin() {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: l => doLogin(l.code, resolve, reject),
      fail: () => doLogin('dev-user-001', resolve, reject)
    })
  })
}

function doLogin(code, resolve, reject) {
  uni.request({
    url: BASE + '/api/mini/auth/login',
    method: 'POST',
    data: { code },
    success: res => {
      const body = res.data
      if (body.code === 0) {
        uni.setStorageSync('token', body.data.token)
        uni.setStorageSync('customerId', body.data.customerId)
        bindStoredPartner()
        resolve(body.data)
      } else {
        reject(new Error(body.msg))
      }
    },
    fail: reject
  })
}

export function capturePartner(options) {
  const query = (options && options.query) || {}
  let code = query.partner || ''
  const scene = query.scene || (options && options.scene) || ''
  if (!code && scene) {
    const text = decodeURIComponent(scene)
    const matched = text.match(/partner=([A-Za-z0-9]+)/)
    code = matched ? matched[1] : (/^[A-Za-z0-9]{4,16}$/.test(text) ? text : '')
  }
  if (code) uni.setStorageSync('partnerCode', String(code).toUpperCase())
  if (uni.getStorageSync('token')) bindStoredPartner()
}

function bindStoredPartner() {
  const code = uni.getStorageSync('partnerCode')
  if (!code || !uni.getStorageSync('token')) return
  request('POST', '/api/mini/partner/visit', { code }, true).catch(() => {})
}

export default {
  get: (url, data, auth) => request('GET', url, data, auth),
  post: (url, data, auth = true) => request('POST', url, data, auth),
  put: (url, data, auth = true) => request('PUT', url, data, auth),
  delete: (url, auth = true) => request('DELETE', url, undefined, auth),
  ensureLogin
}
