const BASE = 'http://localhost:8080'

function request(method, url, data, needAuth) {
  return new Promise((resolve, reject) => {
    if (needAuth && !uni.getStorageSync('token')) {
      return ensureLogin().then(() => request(method, url, data, needAuth)).then(resolve).catch(reject)
    }
    uni.request({
      url: BASE + url,
      method,
      data,
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
        resolve(body.data)
      } else {
        reject(new Error(body.msg))
      }
    },
    fail: reject
  })
}

export default {
  get: (url, data, auth) => request('GET', url, data, auth),
  post: (url, data, auth = true) => request('POST', url, data, auth),
  put: (url, data, auth = true) => request('PUT', url, data, auth),
  ensureLogin
}
