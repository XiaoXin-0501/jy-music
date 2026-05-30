import { visitor } from '../enums.js'
import Home from '@/views/home/index.vue'
const Layout = () => import('@/layout/index.vue')

export default {
	path: '/',
	redirect: '/home',
	component: Layout,
	meta: {
		permission: [visitor],
		cache: true,
	},
	children: [
		{
			path: '/home',
			name: 'Home',
			component: Home,
			meta: {
				permission: [visitor],
				cache: true,
			},
		},
	],
}
