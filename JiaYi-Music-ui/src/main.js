import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useAppStore } from '@/store/appStore'
import 'element-plus/dist/index.css'
import '@/style/index.scss'
import '@/style/reset.scss'
import App from './App.vue'
import router from '@/router/routerGuard.js'

const app = createApp(App)
app.use(createPinia()).use(router)
//根据token恢复登录状态，加载相关信息
const appStore = useAppStore()
const token = localStorage.getItem('token')
await appStore.initAppWithoutToken()
if (token && !appStore.isInited) {
	try {
		await appStore.initAppByToken()
	} catch {
		localStorage.removeItem('token')
	}
}
router.isReady().then(() => {
	app.mount('#app')
})
