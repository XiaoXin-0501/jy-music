import { defineStore } from 'pinia'
import { useSongStore } from './songStore'
import { useSongListStore } from './songListStore'
import { ref } from 'vue'
import { PausableTimer } from '@/utils/common.js'

export const usePlayListStore = defineStore('playListStore', () => {
	const songStore = useSongStore()
	const songListStore = useSongListStore()

	const audio = ref(null) // 音频实例
	const audioCtx = ref(null) // Web Audio 上下文
	const sourceNode = ref(null) // Web Audio 源节点

	const playList = ref([]) // 播放列表
	const currentIndex = ref(0) // 当前歌曲索引
	const currentSong = ref(null) // 当前歌曲对象
	const progress = ref(0) //进度

	const isPlaying = ref(false)
	const currentTime = ref(0)

	//播放10s后自增播放量
	const timer = new PausableTimer(10000)

	const playMode = ref('sequence')
	const mode = {
		SEQUENCE: 'sequence',
		CIRCULATION: 'circulation',
		RANDOM: 'random',
	}

	const init = () => {
		if (audio.value) return

		audio.value = new Audio()
		audio.value.crossOrigin = 'anonymous'

		audioCtx.value = new window.AudioContext({
			latencyHint: 'playback',
		})

		sourceNode.value = audioCtx.value.createMediaElementSource(audio.value)

		sourceNode.value.connect(audioCtx.value.destination)

		audio.value.addEventListener('timeupdate', updateProgress)
		audio.value.addEventListener('ended', playEnd)
	}

	const updateProgress = () => {
		currentTime.value = audio.value.currentTime
		progress.value = (
			(currentTime.value * 100) /
			currentSong.value.duration
		).toFixed(2)
	}

	const playEnd = () => {
		isPlaying.value = false
		timer.reset()
		playNext()
	}

	const playSong = (song, songArray) => {
		if (!song || !songArray) return
		if (song.id === currentSong.value?.id) {
			resume()
			return
		}
		init()
		timer.reset()
		playList.value = songArray
		currentSong.value = song
		const index = playList.value.findIndex(id => id === song.id)
		currentIndex.value = index >= 0 ? index : 0
		audio.value.src = song.songUrl
		audio.value.load()

		if (audioCtx.value.state === 'suspended') {
			audioCtx.value.resume()
		}
		audio.value.play()
		timer.start(() => {
			songStore.updatePlayCount(song.id)
		})
		isPlaying.value = true
	}

	const start = () => {
		if (currentSong.value) return
		const hotSongs = [...songListStore.hotRank.keys()]
		const song = songStore.getSongById(hotSongs[0])
		const songArray = hotSongs
		if (!song || songArray.length === 0) return
		init()
		timer.reset()
		playList.value = songArray
		currentSong.value = song
		const index = playList.value.findIndex(id => id === song.id)
		currentIndex.value = index >= 0 ? index : 0
		audio.value.src = song.songUrl
		audio.value.load()
		timer.start(() => {
			songStore.updatePlayCount(song.id)
		})
		audio.value?.pause()
		timer.pause()
		isPlaying.value = false
		return
	}

	const pause = () => {
		audio.value?.pause()
		timer.pause()
		isPlaying.value = false
	}

	const resume = () => {
		if (audioCtx.value.state === 'suspended') {
			audioCtx.value.resume()
		}
		audio.value?.play()
		if (!timer.timer && currentSong.value) {
			timer.start(() => {
				songStore.updatePlayCount(currentSong.value.id)
			})
		} else {
			timer.resume()
		}
		isPlaying.value = true
	}

	const playPre = () => {
		if (!playList.value) return
		const len = playList.value.length
		if (len === 0) return
		if (len === 1) {
			audio.value.currentTime = 0
			currentTime.value = 0
			audio.value.play()
			return
		}
		currentIndex.value = (currentIndex.value - 1 + len) % len
		const song = songStore.getSongById(playList.value[currentIndex.value])
		playSong(song, playList.value)
	}

	const playNext = () => {
		if (!playList.value) return
		const len = playList.value.length
		if (len === 0) return
		if (len === 1) {
			audio.value.currentTime = 0
			currentTime.value = 0
			audio.value.play()
			return
		}
		currentIndex.value = (currentIndex.value + 1) % len
		const song = songStore.getSongById(playList.value[currentIndex.value])
		console.log(currentIndex.value)
		playSong(song, playList.value)
	}

	const seekTo = percent => {
		if (!currentSong.value) return
		const time = Math.floor(currentSong.value.duration * percent)
		audio.value.currentTime = time
		currentTime.value = time
		progress.value = (percent * 100).toFixed(2)
	}

	return {
		playList,
		currentIndex,
		currentSong,
		isPlaying,
		progress,
		currentTime,
		playSong,
		pause,
		resume,
		seekTo,
		playPre,
		playNext,
		start,
	}
})
