import { defineStore } from "pinia"
import { ref } from "vue"

export const useCacheStore = defineStore("cacheStore", () => {
	const cacheList = ref([])
	const clearCache = () => {
		cacheList.value = []
	}
	const delCache = (name) => {
		cacheList.value = cacheList.value.filter((item) => {
			return item !== name
		})
	}
	const addCache = (name) => {
		// 空值校验 + 去重校验
		if (!name || cacheList.value.includes(name)) return
		cacheList.value.push(name)
	}
	const refreshCache = (name) => {
		delCache(name)
		addCache(name)
	}
	return { cacheList, clearCache, delCache, addCache, refreshCache }
})
