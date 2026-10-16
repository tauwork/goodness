import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 15000 })

http.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token')
  if (token) cfg.headers.Authorization = token
  return cfg
})

http.interceptors.response.use(
  resp => {
    const body = resp.data
    if (body.code === 0) return body.data
    return Promise.reject(new Error(body.msg || '请求失败'))
  },
  err => {
    const body = err.response?.data
    if (body?.code === 2001) {
      localStorage.removeItem('token')
      location.href = '/#/login'
    }
    return Promise.reject(new Error(body?.msg || '网络异常'))
  }
)

export default http
