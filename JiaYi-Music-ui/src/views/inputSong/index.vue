<script setup>
import { useCategoryStore } from '@/store/categoryStore.js'
import { ref, onMounted, reactive, computed, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { message } from '@/utils/message.js'
import { useUserStore } from '@/store/userStore.js'
import { getAudioDuration } from '@/utils/fileUtil/index.js'
import { formatTime, throttle } from '@/utils/common.js'
import clipImg from '@/components/clipImg.vue'
import fileInput from '@/components/fileInput.vue'
import {
	mergeChunksApi,
	mergeLyricsChunksApi,
	lyricsIsUploadApi,
	isUploadApi,
	uploadCoverImgApi,
} from '@/api/song.js'
import { useSongListStore } from '@/store/songListStore.js'
import { useSongStore } from '@/store/songStore'
import { usePlayListStore } from '@/store/playListStore.js'

//命名，否则无法缓存
defineOptions({
	name: 'InputSong',
})
const api = {
	mergeChunksApi,
	isUploadApi,
}

const lyricsApi = {
	mergeChunksApi: mergeLyricsChunksApi,
	isUploadApi: lyricsIsUploadApi,
}
const songId = ref()
const songRef = ref(null)
const addToRef = ref(null)
const lyricsRef = ref(null)
const userStore = useUserStore()
const songStore = useSongStore()
const playListStore = usePlayListStore()
const songListStore = useSongListStore()
const categoryStore = useCategoryStore()
const { categoryList } = storeToRefs(categoryStore)
const { uploadList, favoriteList, otherList } = storeToRefs(songListStore)
const { isPlaying } = storeToRefs(playListStore)
const selectedMap = reactive(new Map())
const clipImgRef = ref(null)
const addListRef = ref(null)
const showAdd = ref(false)
const preImg = ref('')
const addListStyle = ref({})
const formData = ref({
	// 封面
	coverImg: '',
	// 发布账号
	account: userStore.account,
	//歌手
	singer: '未知',
	//歌名
	name: '',
	//歌词
	lyrics: '',
	//歌曲
	songUrl: '',
	//作词人
	lyricist: '未知',
	//作曲人
	composer: '未知',
	//播放时长
	duration: '',
	//分类
	categories: [],
})
const textInputConf = [
	{ label: '歌手', field: 'singer', required: false },
	{ label: '歌名', field: 'name', required: true },
	{ label: '作词人', field: 'lyricist', required: false },
	{ label: '作曲人', field: 'composer', required: false },
]
const uploadSongs = computed(() => {
	songStore.songs
	return uploadList.value.songs
		.map(id => songStore.getSongById(id))
		.filter(Boolean)
})

const submitCoverImg = async (image, blob) => {
	const reader = new FileReader()
	reader.onload = () => {
		preImg.value = reader.result
	}
	reader.readAsDataURL(blob)
	//上传封面图，获取网络地址
	const fd = new FormData()
	fd.append('coverImg', blob)
	const data = await uploadCoverImgApi(fd)
	formData.value.coverImg = data.data.coverImg
	return true
}

const selectLabel = id => {
	if (selectedMap.has(id)) {
		selectedMap.delete(id)
	} else {
		const target = categoryList.value.find(item => item.id === id)
		const name = target.name
		selectedMap.set(id, name)
	}
}

const getSong = async (url, file) => {
	formData.value.songUrl = url
	formData.value.duration = Math.floor(await getAudioDuration(url))
	console.log(formData.value)
}
const getLyrics = (url, file) => {
	formData.value.lyrics = url
}

const submitData = throttle(async () => {
	if (!songId.value) {
		formData.value.categories = Array.from(selectedMap.keys())
		if (!valiate()) return
		const res = await songStore.uploadSong(formData.value)
		if (res) {
			message('上传成功', 'success')
			clearAll()
		}
	} else {
		if (!valiate()) return
		const res = await songStore.updateSong(songId.value, formData.value)
		if (res) {
			message('更新成功', 'success')
			clearAll()
		}
	}
}, 500)

const updateSong = data => {
	clearAll()
	Object.keys(formData.value).forEach(key => {
		if (data[key] !== undefined) {
			formData.value[key] = data[key]
		}
	})
	preImg.value = data.coverImg
	data.categories.forEach(id => {
		selectLabel(id)
	})
	songRef.value.updateFile(data.songUrl)
	lyricsRef.value.updateFile(data.lyrics)
	songId.value = data.id
}

const deleteSong = throttle(async id => {
	const res = await songStore.deleteSong(id)
	if (res) message('删除成功', 'success')
}, 500)

const addToList = async list => {
	try {
		const toListId = list.id
		await songListStore.addSongtoList(toListId, songId.value)
		message('添加成功', 'success')
	} catch (e) {
		console.log(e)
	}
}

const showAddList = (e, song) => {
	songId.value = song.id
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
	showAdd.value = true
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
	} catch (e) {
		message('收藏出错', 'error')
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
	playListStore.playSong(song, uploadList.value.songs)
}

const clearAll = () => {
	formData.value = {
		coverImg: '',
		account: userStore.account,
		singer: '未知',
		name: '',
		lyrics: '',
		songUrl: '',
		lyricist: '未知',
		composer: '未知',
		duration: '',
		categories: [],
	}
	selectedMap.clear()
	songId.value = ''
	preImg.value = ''
	songRef.value.initUploadState()
	lyricsRef.value.initUploadState()
}
const valiate = () => {
	if (!formData.value.coverImg) {
		message('请先上传封面图', 'warning')
		return false
	} else if (!formData.value.songUrl) {
		message('请先上传歌曲', 'warning')
		return false
	} else if (!formData.value.name) {
		message('请先填写歌名', 'warning')
		return false
	}
	return true
}
const handleClose = e => {
	if (!showAdd.value) return
	if (addListRef.value.contains(e.target)) return
	if (addToRef.value.some(el => el.contains(e.target))) return
	if (showAdd.value) showAdd.value = false
}
onMounted(() => {
	document.addEventListener('click', handleClose)
})
onUnmounted(() => {
	document.removeEventListener('click', handleClose)
})
</script>
<template>
	<clipImg ref="clipImgRef" :submit="submitCoverImg"></clipImg>
	<div class="inputSong_container">
		<form class="input_container">
			<ul
				class="addList"
				ref="addListRef"
				:style="addListStyle"
				v-show="showAdd"
			>
				<li v-for="item in [...otherList.values()]" @click="addToList(item)">
					<div class="name">{{ item.name }}</div>
				</li>
			</ul>
			<div class="coverImg" @click="clipImgRef.showClipImg = true">
				<div class="mark" v-if="!preImg">
					<i class="iconfont icon-jiahao"></i>
				</div>
				<img :src="preImg" v-else />
			</div>
			<div class="fileInput">
				<fileInput
					ref="songRef"
					:api="api"
					:type="['.mp3', '.mp4']"
					@handleRes="getSong"
				>
					<template #label> 歌曲：</template>
				</fileInput>
				<fileInput
					ref="lyricsRef"
					:api="lyricsApi"
					:type="['.lrc']"
					@handleRes="getLyrics"
				>
					<template #label> 歌词：</template>
				</fileInput>
			</div>
			<div class="textInput">
				<label v-for="item in textInputConf">
					{{ item.label + ':' }}
					<input type="text" v-model="formData[item.field]" />
				</label>
			</div>
			<div class="categoryInput">
				<div class="selected">
					<div class="title">选择标签:</div>
					<div
						class="label label-selected"
						v-for="item in selectedMap.entries()"
						:key="item[0]"
						@click="selectedMap.delete(item[0])"
					>
						{{ item[1] }}
					</div>
				</div>
				<div class="list">
					<div
						v-for="item in categoryList"
						class="label"
						:class="{ 'label-selected': selectedMap.has(item.id) }"
						:key="item.id"
						@click="selectLabel(item.id)"
					>
						{{ item.name }}
					</div>
				</div>
			</div>
			<div class="submit">
				<i
					class="iconfont icon-document-handle-cancel"
					title="取消"
					@click="clearAll"
				></i>
				<i class="iconfont icon-top" title="提交" @click="submitData"></i>
			</div>
		</form>
		<div class="myInput_container">
			<div class="title">我的上传</div>
			<div class="list">
				<div class="list_title">
					<p class="song_name">歌名/歌手</p>
					<p class="date">上传时间</p>
					<p class="duration">时长</p>
				</div>
				<div class="list_item" v-for="item in uploadSongs">
					<div class="item_info">
						<div class="playMark" @click="handlePlay(item)">
							<i
								class="iconfont icon-pause"
								v-if="isPlaying && playListStore.currentSong.id === item.id"
							></i>

							<i class="iconfont icon-play" v-else></i>
						</div>
						<img :src="item.coverImg" alt="" />
						<p class="song_name">{{ item.name }}</p>
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
							title="删除"
							@click="deleteSong(item.id)"
						></i>
						<i
							class="iconfont icon-tianjiazengjiajia"
							title="添加到"
							ref="addToRef"
							@click="e => showAddList(e, item)"
						></i>
						<i
							class="iconfont icon-bianji"
							title="编辑"
							@click="updateSong(item)"
						></i>
					</div>
					<div class="item_date">
						<p>{{ item.createTime?.slice(0, 10) || '' }}</p>
					</div>
					<div class="item_duration">
						<p>{{ item.duration ? formatTime(item.duration) : '' }}</p>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>
<style scoped>
.inputSong_container {
	display: flex;
	flex-direction: column;
	width: 100%;
	height: 100%;
	box-sizing: border-box;
	padding: var(--main-padding);
	padding-top: 0;
	border-radius: 0 0 var(--main-border-radius) var(--main-border-radius);
}
.input_container {
	display: flex;
	width: 100%;
	height: 150px;
	flex: 0 0 150px;
}
.coverImg {
	width: 150px;
	height: 150px;
	min-width: 150px;
}
.coverImg .mark {
	display: flex;
	justify-content: center;
	align-items: center;
	width: 100%;
	height: 100%;
	box-sizing: border-box;
	border: 1px dashed rgb(150, 150, 150);
	border-radius: 5px;
	cursor: pointer;
	color: rgb(150, 150, 150);
}
.coverImg .mark:hover {
	background-color: rgb(236, 245, 255);
	border: 1px dashed rgb(114, 183, 255);
	color: rgb(114, 183, 255);
}
.mark .icon-jiahao {
	font-size: 80px;
}
.coverImg img {
	width: 100%;
	height: 100%;
	box-sizing: border-box;
	border-radius: 5%;
	cursor: pointer;
}
.coverImg img:hover {
	border: 1px dashed rgb(114, 183, 255);
}
.fileInput {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	height: 100%;
	padding: 10px;
	box-sizing: border-box;
}
.textInput {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	height: 100%;
	padding: 0 10px;
}
.textInput input {
	border: none;
	outline: none;
	background-color: transparent;
	border-bottom: 1px solid rgba(100, 100, 100, 0.5);
}
.categoryInput {
	display: flex;
	flex-direction: column;
	height: 100%;
	width: 300px;
	border: 1px solid rgba(200, 200, 200, 1);
	border-radius: 5px;
	margin: 0 10px;
	box-sizing: border-box;
}
.categoryInput .selected {
	display: flex;
	flex-wrap: wrap;
	width: 100%;
	height: 45%;
	padding: 2px;
	border-radius: 5px 0;
	border-bottom: 1px dashed rgb(150, 150, 150);
	box-sizing: border-box;
	overflow-y: auto;
	overflow-x: hidden;
}
.categoryInput .list {
	display: flex;
	flex-wrap: wrap;
	width: 100%;
	height: 55%;
	padding: 2px;
	border-radius: 0 5px;
	box-sizing: border-box;
	overflow-y: auto;
	overflow-x: hidden;
}
.label {
	position: relative;
	height: 24px;
	padding: 3px 10px;
	border-radius: 10px;
	border: 1px solid gray;
	margin: 3px;
	box-sizing: border-box;
	cursor: pointer;
}
.label:hover {
	background-color: rgb(200, 200, 200);
}
.label-selected {
	background-color: rgb(31, 216, 31);
	color: white;
}
.title {
	padding: 6px;
}
.submit {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	height: 100%;
	padding: 10px;
	box-sizing: border-box;
}
.icon-document-handle-cancel,
.icon-top {
	font-size: 20px;
	color: rgb(180, 180, 180);
	cursor: pointer;
}
.icon-top:hover {
	color: rgb(75, 175, 190);
}
.icon-document-handle-cancel:hover {
	color: rgb(198, 79, 36);
}
.myInput_container {
	flex: 1;
	display: flex;
	flex-direction: column;
	min-height: 0;
}
.myInput_container .title {
	padding: 15px 0;
	font-size: 20px;
}
.myInput_container .list {
	position: relative;
	flex: 1;
	box-sizing: border-box;
	overflow: hidden;
	overflow-y: auto;
}
.list .list_title,
.list .list_item {
	display: grid;
	grid-template-columns: 2fr 200px 2fr 1fr;
	gap: 50px;
	padding: 3px 5px;
	border-radius: 5px;
}
.list_title {
	position: sticky;
	z-index: var(--z-index-top);
	left: 0;
	top: 0;
	background-color: var(--bc-main-body);
}
.list_title .song_name {
	grid-column: span 2;
}
.list_item:hover .playMark {
	display: grid;
}
.list .list_item:hover {
	background-color: rgb(220, 220, 220);
	cursor: pointer;
}
.item_info {
	position: relative;
	display: grid;
	grid-template-columns: 80px 1fr;
	grid-template-rows: 40px 40px;
	padding: 3px;
}
.playMark {
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
.playMark .icon-play {
	font-size: 30px;
	color: rgb(25, 25, 25);
}
.playMark .icon-pause {
	font-size: 30px;
	color: rgb(25, 25, 25);
}

.item_info img {
	grid-row: span 2;
	border-radius: 5px;
	width: 100%;
	height: 100%;
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
.item_date,
.item_duration {
	display: flex;
	align-items: center;
}
.list_item:nth-child(even) {
	background: rgb(240, 240, 240);
}
.icon-bianji:hover {
	color: rgb(129, 234, 238);
}
.icon-shanchu:hover {
	color: rgb(198, 79, 36);
}
.iconfont.icon-aixin {
	color: red;
}
.iconfont.icon-xihuan:hover {
	color: red;
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
