import { storeToRefs } from 'pinia'
import { router } from './index.js'
import { useAppStore } from '@/store/appStore.js'
import { useUserStore } from '@/store/userStore.js'
import { useCacheStore } from '@/store/cacheStore.js'
import { message } from '@/utils/message.js'
import { user_common, user_vip } from './enums.js'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

router.beforeEach(async (to, from, next) => {
	const cacheStore = useCacheStore()
	/**
	 * 权限验证
	 */
	const userStore = useUserStore()
	const roles = userStore.roles

	if (hasPermission(to.meta.permission, roles)) {
		NProgress.start()
		if (to.meta.cache) {
			cacheStore.addCache(to.name)
		}
		next()
	} else {
		if (from.path === '/') {
			next('/home')
		} else {
			if (to.meta.permission.includes(user_common)) {
				message('请先登录', 'warning')
			} else if (to.meta.permission.includes(user_vip)) {
				message('请先成为vip', 'warning')
			}
			next(false)
		}
	}
})

router.afterEach(to => {
	NProgress.done()
})

//role与permission没有交集则没有权限
const hasPermission = (permission, roles) => {
	if (!permission || permission.length === 0) return true
	return permission.some(item => roles.includes(item))
}

export default router
