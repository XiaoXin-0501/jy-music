import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategortListApi } from '@/api/song'

export const useCategoryStore = defineStore('categoryStore', () => {
	const categoryList = ref([])

	const getCategoryList = async () => {
		const res = await getCategortListApi()
		categoryList.value = res.data
	}
	return { categoryList, getCategoryList }
})
