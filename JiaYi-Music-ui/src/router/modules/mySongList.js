import { user_admin, user_vip, user_common } from '../enums.js'
import MySongList from '@/views/mySongList/index.vue'
const Layout = () => import('@/layout/index.vue')

export default {
	path: '/',
	redirect: '/home',
	component: Layout,
	children: [
		{
			path: '/mySongList/:id',
			name: 'MySongList',
			component: MySongList,
			meta: {
				permission: [user_admin, user_vip, user_common],
				cache: true,
			},
		},
	],
}
