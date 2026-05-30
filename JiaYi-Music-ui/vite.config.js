import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {
	ElementPlusResolver,
	ArcoResolver,
} from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
	plugins: [
		AutoImport({
			resolvers: [ElementPlusResolver(), ArcoResolver()],
		}),
		Components({
			resolvers: [
				ElementPlusResolver(),
				ArcoResolver({
					sideEffect: true,
				}),
			],
		}),
		vue(),
	],
	resolve: {
		alias: {
			'@': path.resolve(__dirname, 'src'),
		},
	},
})
