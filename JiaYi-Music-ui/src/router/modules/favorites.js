import { user_vip, user_admin, user_common } from '../enums.js'
import Favorite from '@/views/favorite/index.vue'
const Layout = () => import('@/layout/index.vue')

export default {
	path: '/',
	redirect: '/home',
	component: Layout,
	children: [
		{
			path: '/favorite',
			name: 'Favorite',
			component: Favorite,
			meta: {
				permission: [user_admin, user_vip, user_common],
				cache: true,
			},
		},
	],
}
