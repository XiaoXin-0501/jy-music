import { user_admin, user_vip, user_common } from '../enums.js'
import MyInfo from '@/views/myInfo/index.vue'
const Layout = () => import('@/layout/index.vue')

export default {
	path: '/',
	redirect: '/home',
	component: Layout,
	children: [
		{
			path: '/myInfo',
			name: 'MyInfo',
			component: MyInfo,
			meta: {
				permission: [user_admin, user_vip, user_common],
				cache: true,
			},
		},
	],
}
