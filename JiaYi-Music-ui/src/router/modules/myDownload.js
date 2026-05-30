import { user_admin, user_vip, user_common } from '../enums.js'
import MyDownload from '@/views/myDownload/index.vue'
const Layout = () => import('@/layout/index.vue')

export default {
	path: '/',
	redirect: '/home',
	component: Layout,
	children: [
		{
			path: '/myDownload',
			name: 'MyDownload',
			component: MyDownload,
			meta: {
				permission: [user_admin, user_vip, user_common],
				cache: true,
			},
		},
	],
}
