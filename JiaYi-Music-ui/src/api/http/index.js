import axios from 'axios'
import { message } from '@/utils/message.js'
const http = axios.create({
	baseURL: 'http://localhost:8080',
	timeout: 10000,
	withCredentials: true,
	// headers: { "X-Custom-Header": "foobar" },
	// headers: { 'Content-Type': 'application/json;charset=UTF-8' },
})

//请求白名单，不需要携带token
const whiteList = [
	'/login',
	'/captcha',
	'/song/getPlayRank',
	'/song/getCollectRank',
	'/song/getHotRank',
	'/song/getRankSongs',
]
// 存储所有请求控制器
const controllers = new Set()
//注册
const addController = controller => {
	controllers.add(controller)
}
//移除
const removeController = element => {
	if (element instanceof Set) {
		element.forEach(item => controllers.delete(item))
	} else {
		controllers.delete(element)
	}
}
//取消所有未完成的请求
const cancelAllRequest = () => {
	controllers.forEach(controller => controller.abort())
	controllers.clear()
}
// 添加请求拦截器
http.interceptors.request.use(
	function (config) {
		console.log('requestConfig', config)
		//外部想局部控制，需要而外把控制器交给全局
		if (!config.signal) {
			const controller = new AbortController()
			config.signal = controller.signal
			addController(controller)
			config._controller = controller
		}
		if (isWhite(config)) {
			return config
		} else {
			const token = localStorage.getItem('token')
			if (token) {
				config.headers.Authorization = token
				return config
			} else {
				message('登陆失效', 'error')
				return Promise.reject(new Error('登录失效'))
			}
		}
	},
	function (error) {
		// 对请求错误做些什么
		message('请求失败', 'error')
		return Promise.reject(error)
	}
)

// 添加响应拦截器
http.interceptors.response.use(
	function (response) {
		// 请求完成 → 移除controller(是否成功都移除)
		if (response.config._controller)
			removeController(response.config._controller)
		if (response.data.code && response.data.code != 2000) {
			message(response.data.msg, 'error')
			return Promise.reject({
				msg: response.message,
				data: response.data,
			})
		}
		console.log('responseSuccess', response.data)
		if (!response.data.code) return response.headers
		return response.data
	},
	function (error) {
		console.log('responseError', error)
		// 超出 2xx 范围的状态码都会触发该函数。
		//手动取消的请求不打印信息
		if (!axios.isCancel(error)) message('请求失败', 'error')
		controllers.delete(error.config._controller)
		return Promise.reject(error)
	}
)

const isWhite = config => {
	const requestUrl = config.url || ''
	return whiteList.some(path => {
		if (requestUrl === path) return true
		if (requestUrl.endsWith(path)) return true
		return false
	})
}

export { http, cancelAllRequest, addController, removeController }
