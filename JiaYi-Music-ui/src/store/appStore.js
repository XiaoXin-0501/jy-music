import { defineStore } from 'pinia'
import { useCategoryStore } from './categoryStore.js'
import { useUserStore } from './userStore.js'
import { useSongStore } from './songStore.js'
import { useSongListStore } from './songListStore.js'
import { usePlayListStore } from './playListStore.js'
import { ref } from 'vue'
import { cancelAllRequest } from '@/api/http/index.js'
import WebSocketClient from '@/utils/webSocket/index.js'

export const useAppStore = defineStore('app', () => {
	const isInited = ref(false)
	const userStore = useUserStore()
	const categoryStore = useCategoryStore()
	const songListStore = useSongListStore()
	const songStore = useSongStore()
	const playListStore = usePlayListStore()
	// 全局初始化：登录后一次性加载所有基础数据
	const initAppByToken = async () => {
		try {
			await Promise.all([
				userStore.getUserInfo(),
				categoryStore.getCategoryList(),
				songListStore.getSongList(),
				songStore.getSongs(),
			])
			WebSocketClient.getInstance(
				`ws://localhost:8080/ws/${userStore.userId}`
			).connect()
			isInited.value = true
		} catch (e) {
			cancelAllRequest()
			throw e
		}
	}

	const initAppWithoutToken = async () => {
		try {
			await Promise.all([
				songListStore.getPlayRank(),
				songListStore.getCollectRank(),
				songListStore.getHotRank(),
				songStore.getRankSongs(),
			])
			playListStore.start()
		} catch (e) {
			cancelAllRequest()
			throw e
		}
	}

	return { isInited, initAppByToken, initAppWithoutToken }
})
