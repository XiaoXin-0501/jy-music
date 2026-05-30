<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { formatTime } from '@/utils/common.js'
import functionList from './functionList.vue'
import { storeToRefs } from 'pinia'
import { useSongStore } from '@/store/songStore'
import { useSongListStore } from '@/store/songListStore.js'
import { usePlayListStore } from '@/store/playListStore.js'
import { message } from '@/utils/message'

const propos = defineProps({
	listData: Object,
})
const playListStore = usePlayListStore()
const songListStore = useSongListStore()
const songStore = useSongStore()
const songs = computed(() => {
	songStore.songs
	return propos.listData.songs
		.map(id => songStore.getSongById(id))
		.filter(Boolean)
})
const { favoriteList, otherList } = storeToRefs(songListStore)
const { isPlaying } = storeToRefs(playListStore)
const showAdd = ref(false)
const showFunc = ref(false)
const funcListRef = ref(null)
const moreRef = ref(null)
const addListRef = ref(null)
const addToRef = ref(null)
const currentSong = ref(null)
const funcListStyle = ref({})
const addListStyle = ref({})

const addToList = async list => {
	try {
		const toListId = list.id
		const songId = currentSong.value.id
		await songListStore.addSongtoList(toListId, songId)
		message('添加成功', 'success')
	} catch (e) {
		console.log(e)
	}
}

const showFuncList = (e, song) => {
	currentSong.value = song
	e.preventDefault()
	const winH = window.innerHeight
	const clientY = e.clientY
	if (clientY > winH / 2) {
		funcListStyle.value = {
			bottom: winH - clientY + 'px',
			left: e.clientX + 'px',
		}
	} else {
		funcListStyle.value = {
			top: clientY + 'px',
			left: e.clientX + 'px',
		}
	}
	showAdd.value = false
	showFunc.value = true
}
const showAddList = (e, song) => {
	currentSong.value = song
	e.preventDefault()
	const winH = window.innerHeight
	const clientY = e.clientY
	if (clientY > winH / 2) {
		addListStyle.value = {
			bottom: winH - clientY + 'px',
			left: e.clientX + 'px',
		}
	} else {
		addListStyle.value = {
			top: clientY + 'px',
			left: e.clientX + 'px',
		}
	}
	showFunc.value = false
	showAdd.value = true
}
const handleClose = e => {
	if (!showAdd.value && !showFunc.value) return
	if (funcListRef.value.$el.contains(e.target)) return
	if (addListRef.value.contains(e.target)) return
	if (addToRef.value.some(el => el.contains(e.target))) return
	if (moreRef.value.some(el => el.contains(e.target))) return

	if (showAdd.value) showAdd.value = false
	if (showFunc.value) showFunc.value = false

	currentSong.value = null
}
const handleFavorite = async song => {
	try {
		if (!songListStore.isFavorite(song.id)) {
			const toListId = favoriteList.value.id
			await songListStore.addSongtoList(toListId, song.id)
			message('收藏成功', 'success')
		} else {
			await songListStore.removeSongFromList(song.id, favoriteList.value.id)
			message('取消收藏成功', 'success')
		}
		showFunc.value = false
	} catch (e) {
		console.log(e)
	}
}

const handlePlay = song => {
	if (song.id === playListStore.currentSong?.id) {
		if (isPlaying.value) {
			playListStore.pause()
		} else {
			playListStore.resume()
		}
		return
	}
	playListStore.playSong(song, propos.listData.songs)
}

const deleteSong = async song => {
	const fromListId = propos.listData.id
	await songListStore.removeSongFromList(song.id, fromListId)
	message('删除成功', 'success')
}

const downloadSong = async song => {
	await songStore.downloadSong(song.id)
}

onMounted(() => {
	document.addEventListener('click', handleClose)
})
onUnmounted(() => {
	document.removeEventListener('click', handleClose)
})
</script>
<template>
	<div class="list_container">
		<functionList
			v-show="showFunc"
			ref="funcListRef"
			class="functionList"
			:style="funcListStyle"
			:song="currentSong"
			:listId="listData.id"
			@favorite="handleFavorite"
			@add="addToList"
			@delete="deleteSong"
		></functionList>
		<ul class="addList" ref="addListRef" :style="addListStyle" v-show="showAdd">
			<li v-for="item in [...otherList.values()]" @click="addToList(item)">
				<div class="name">{{ item.name }}</div>
			</li>
		</ul>
		<div class="list_title">
			<p class="songName">歌名/歌手</p>
			<p>上传时间</p>
			<p>时长</p>
		</div>
		<div
			class="list_item"
			@contextmenu="e => showFuncList(e, item)"
			v-for="item in songs"
		>
			<div class="item_info">
				<div class="mark" @click="handlePlay(item)">
					<i
						class="iconfont icon-pause"
						v-if="isPlaying && playListStore.currentSong.id === item.id"
					></i>

					<i class="iconfont icon-play" v-else></i>
				</div>
				<img :src="item.coverImg" alt="" class="coverImg" />
				<p class="name">{{ item.name }}</p>
				<p class="singer">{{ item.singer }}</p>
			</div>
			<div class="item_function">
				<i
					class="iconfont icon-xihuan"
					title="喜欢"
					v-if="!songListStore.isFavorite(item.id)"
					@click="handleFavorite(item)"
				></i>
				<i
					class="iconfont icon-aixin"
					title="喜欢"
					v-else
					@click="handleFavorite(item)"
				></i>
				<i
					class="iconfont icon-shanchu"
					v-if="listData?.id === songListStore.downloadList.id"
					@click="deleteSong(item)"
				></i>
				<i
					class="iconfont icon-xiazai"
					v-else
					@click="downloadSong(item)"
					title="下载"
				></i>
				<i
					class="iconfont icon-tianjiazengjiajia"
					ref="addToRef"
					title="添加到"
					@click="e => showAddList(e, item)"
				></i>
				<i
					class="iconfont icon-gengduo"
					ref="moreRef"
					title="更多"
					@click="e => showFuncList(e, item)"
				></i>
			</div>
			<div class="createDate">
				<p>{{ item.createTime?.slice(0, 10) || '' }}</p>
			</div>
			<div class="duration">
				<p>{{ item.duration ? formatTime(item.duration) : '' }}</p>
			</div>
		</div>
	</div>
</template>
<style scoped>
.list_container {
	display: flex;
	flex-direction: column;
	width: 100%;
	height: 100%;
	border-radius: 5px;
	box-sizing: border-box;
	overflow: hidden;
	overflow-y: auto;
}
.list_title,
.list_item {
	position: relative;
	display: grid;
	grid-template-columns: 2fr 200px 2fr 1fr;
	gap: 50px;
	padding: 3px 5px;
	border-radius: 5px;
	cursor: pointer;
}
.list_item:nth-child(even) {
	background: rgb(240, 240, 240);
}
.list_item:hover {
	display: grid;
	background: rgb(220, 220, 220);
}
.list_item:hover .mark {
	display: grid;
}
.functionList {
	position: fixed;
}
.list_title {
	position: sticky;
	left: 0;
	top: 0;
	background-color: var(--bc-main-body);
	z-index: var(--z-index-second);
}
.songName {
	grid-column: span 2;
}
.item_info {
	position: relative;
	display: grid;
	grid-template-columns: 80px 1fr;
	grid-template-rows: 40px 40px;
	padding: 3px;
}
.mark {
	display: none;
	justify-content: center;
	align-items: center;
	position: absolute;
	top: 3px;
	left: 3px;
	width: 80px;
	height: 80px;
	border-radius: 5px;
	background-color: rgba(100, 100, 100, 0.5);
}
.mark .icon-play {
	font-size: 30px;
	color: rgb(25, 25, 25);
}
.mark .icon-pause {
	font-size: 30px;
	color: rgb(25, 25, 25);
}
.coverImg {
	width: 100%;
	height: 100%;
	border-radius: 5px;
	grid-row: span 2;
}
.item_info p {
	padding-left: 15px;
}
.item_function {
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.item_function .iconfont {
	font-size: 20px;
	color: rgb(150, 150, 150);
	cursor: pointer;
}
.item_function .iconfont:hover {
	color: rgb(129, 234, 238);
}
.item_function .icon-xihuan:hover {
	color: red;
}
.iconfont.icon-aixin {
	color: red;
}
.iconfont.icon-aixin:hover {
	color: brown;
}
.createDate,
.duration {
	display: flex;
	align-items: center;
}
.addList {
	position: absolute;
	z-index: var(--z-index-top);
	width: 200px;
	border-radius: 5px;
	background-color: white;
	box-shadow: 0 0 10px 0 rgb(200, 200, 200);
}
.addList li {
	padding: 10px;
	border-radius: 5px;
	cursor: pointer;
}
.addList li:hover {
	background-color: rgb(200, 200, 200);
}
</style>
