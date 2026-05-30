import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
	getSongListApi,
	addSongToListApi,
	removeSongFromListApi,
	createSongListApi,
	deleteSongListApi,
	uploadListCoverImgApi,
	updateListApi,
	getPlayRankApi,
	getCollectRankApi,
	getHotRankApi,
} from '@/api/songList.js'
import defaultCoverImg from '@/assets/png/defaultCoverImg.png'

export const useSongListStore = defineStore('songListStore', () => {
	const uploadList = ref(null)
	const favoriteList = ref(null)
	const downloadList = ref(null)
	const otherList = ref(new Map())
	//用于判断歌曲是否为"我喜欢",普通数组性能差于set
	const favoriteIds = ref(new Set())

	const playRank = ref(new Map())
	const collectRank = ref(new Map())
	const hotRank = ref(new Map())
	const getSongList = async () => {
		try {
			const res = await getSongListApi()
			const list = res.data || []
			// 遍历分类
			list.forEach(item => {
				switch (item.name) {
					case '我的喜欢':
						favoriteList.value = item
						favoriteIds.value = new Set(item.songs || [])
						break
					case '我的上传':
						uploadList.value = item
						break
					case '我的下载':
						downloadList.value = item
						break
					default:
						item.coverImg = item.coverImg || defaultCoverImg
						otherList.value.set(item.id, item)
						break
				}
			})
		} catch (e) {
			console.error('获取歌单失败：', e)
		}
	}

	const getPlayRank = async () => {
		try {
			const res = await getPlayRankApi()
			playRank.value = new Map(
				Object.entries(res.data).sort((a, b) => b[1] - a[1])
			)
		} catch (e) {
			console.error('获取播放排行榜失败：', e)
		}
	}

	const getCollectRank = async () => {
		try {
			const res = await getCollectRankApi()
			collectRank.value = new Map(
				Object.entries(res.data).sort((a, b) => b[1] - a[1])
			)
		} catch (e) {
			console.error('获取收藏排行榜失败：', e)
		}
	}

	const getHotRank = async () => {
		try {
			const res = await getHotRankApi()
			hotRank.value = new Map(
				Object.entries(res.data).sort((a, b) => b[1] - a[1])
			)
		} catch (e) {
			console.error('获取热度排行榜失败：', e)
		}
	}

	const addSongtoList = async (toListId, songId) => {
		const list = getListById(toListId)
		if (!list) return false
		const isExist = list.songs.some(id => id === songId)
		if (isExist) return false
		await addSongToListApi(toListId, songId)
		if (favoriteIds.value && list.id === favoriteList.value.id) {
			favoriteIds.value.add(songId)
		}
		list.songs.push(songId)
		return true
	}

	const removeSongFromList = async (songId, listId) => {
		await removeSongFromListApi(songId, listId)
		const list = getListById(listId)
		const isExist = list.songs.some(id => id === songId)
		if (isExist) {
			if (favoriteIds.value && list.id === favoriteList.value.id) {
				favoriteIds.value.delete(songId)
			}
			list.songs = list.songs.filter(id => id !== songId)
			return true
		}
		return false
	}

	const createSongList = async name => {
		const res = await createSongListApi(name)
		const id = res.data
		const newSongList = {
			createTime: new Date().toISOString().slice(0, 10),
			id,
			name,
			songs: [],
			coverImg: defaultCoverImg,
		}
		otherList.value.set(id, newSongList)
	}

	const deleteSongList = async listId => {
		try {
			await deleteSongListApi(listId)
			otherList.value.delete(listId)
			return true
		} catch (e) {
			console.log(e)
			return false
		}
	}

	const uploadListCoverImg = async coverImg => {
		try {
			const res = await uploadListCoverImgApi(coverImg)
			return res.data
		} catch (e) {
			console.log(e)
		}
	}

	const updateList = async formData => {
		try {
			let data = {}
			if (formData.coverImg === defaultCoverImg) {
				data = { ...formData, coverImg: '' }
			} else {
				data = formData
			}
			await updateListApi(data)
			const list = otherList.value.get(formData.id)
			list.name = formData.name
			list.coverImg = formData.coverImg ? formData.coverImg : defaultCoverImg
			console.log(list)
		} catch (e) {
			console.log(e)
		}
	}

	const getListById = id => {
		if (uploadList.value?.id === id) return uploadList.value
		if (favoriteList.value?.id === id) return favoriteList.value
		if (downloadList.value?.id === id) return downloadList.value
		return otherList.value.get(id)
	}

	const isFavorite = songId => {
		return favoriteIds.value.has(songId)
	}

	return {
		updateList,
		uploadListCoverImg,
		getListById,
		uploadList,
		favoriteList,
		downloadList,
		otherList,
		playRank,
		hotRank,
		collectRank,
		deleteSongList,
		getSongList,
		addSongtoList,
		isFavorite,
		removeSongFromList,
		createSongList,
		getPlayRank,
		getCollectRank,
		getHotRank,
	}
})
