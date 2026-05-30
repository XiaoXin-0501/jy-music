import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
	uploadSongApi,
	updateSongApi,
	deleteSongApi,
	getSongsApi,
	updatePlayCountApi,
	getRankSongsApi,
} from '@/api/song.js'
import { useSongListStore } from './songListStore'

export const useSongStore = defineStore('songStore', () => {
	const songListStore = useSongListStore()
	const songs = ref(new Map())

	const getSongs = async () => {
		try {
			const res = await getSongsApi()
			res.data.forEach(song => {
				songs.value.set(song.id, song)
			})
		} catch (error) {
			console.log(error)
		}
	}

	const getRankSongs = async () => {
		try {
			const res = await getRankSongsApi()
			res.data.forEach(song => {
				songs.value.set(song.id, song)
			})
		} catch (error) {
			console.log(error)
		}
	}

	const uploadSong = async formData => {
		try {
			const res = await uploadSongApi(formData)
			const id = res.data
			formData.id = id
			formData.createTime = new Date().toISOString().slice(0, 10)
			songs.value.set(id, formData)
			songListStore.uploadList.songs.push(id)
			return true
		} catch (error) {
			console.log(error)
		}
	}

	const updateSong = async (songId, formData) => {
		try {
			await updateSongApi(songId, formData)
			const oldSong = songs.value.get(songId)
			const newSong = { ...oldSong, ...formData }
			songs.value.set(songId, newSong)
			return true
		} catch (e) {
			console.error('更新歌曲失败：', e)
			return false
		}
	}

	const deleteSong = async songId => {
		try {
			await deleteSongApi(songId)
			return songs.value.delete(songId)
		} catch (e) {
			console.error('删除歌曲失败：', e)
			return false
		}
	}

	const updatePlayCount = async songId => {
		try {
			await updatePlayCountApi(songId)
			const song = songs.value.get(songId)
			if (song) {
				song.playCount = (song.playCount || 0) + 1
				// songs.value.set(songId, song)
			}
		} catch (e) {
			console.error('更新播放次数失败：', e)
		}
	}

	const getSongById = songId => {
		return songs.value.get(songId)
	}

	const downloadSong = async songId => {
		const a = document.createElement('a')
		a.href = `http://localhost:8080/song/download/${songId}`
		a.download = ''
		a.target = '_blank'
		document.body.appendChild(a)
		a.click()
		document.body.removeChild(a)
		await songListStore.addSongtoList(songListStore.downloadList.id, songId)
	}

	return {
		songs,
		getSongById,
		getSongs,
		uploadSong,
		updateSong,
		deleteSong,
		downloadSong,
		updatePlayCount,
		getRankSongs,
	}
})
