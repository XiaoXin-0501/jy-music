import { createRouter, createWebHashHistory } from "vue-router"
// 自动导入全部静态路由，无需再手动引入！匹配 src/router/modules 目录（任何嵌套级别）中具有 .ts 扩展名的所有文件，除了 remaining.ts 文件
const modules = import.meta.glob(
	// ["./modules/**/*.js", "!./modules/**/remaining.ts"],
	["./modules/**/*.js"],
	{
		eager: true,
	},
)
/** 原始静态路由（未做任何处理） */
const routes = []

Object.keys(modules).forEach((key) => {
	routes.push(modules[key].default)
})

// 彻底扁平化路由数组（处理多维数组嵌套问题）
const flatRoutes = routes.flat(Infinity)
/** 创建路由实例 */
export const router = createRouter({
	history: createWebHashHistory(),
	routes: flatRoutes,
	strict: true,
	scrollBehavior(to, from, savedPosition) {
		if (savedPosition) {
			return savedPosition
		} else {
			return { top: 0 }
		}
	},
})
