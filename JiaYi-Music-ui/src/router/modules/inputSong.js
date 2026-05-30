import { user_admin, user_vip, user_common } from '../enums.js'
import InputSong from '@/views/inputSong/index.vue'
const Layout = () => import('@/layout/index.vue')

export default {
	path: '/',
	redirect: '/home',
	component: Layout,
	children: [
		{
			path: '/inputSong',
			name: 'InputSong',
			component: InputSong,
			meta: {
				permission: [user_admin, user_vip, user_common],
				cache: true,
			},
		},
	],
}
