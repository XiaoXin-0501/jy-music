<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useSongListStore } from '@/store/songListStore'
import { computed, reactive, ref, watch, onMounted, onUnmounted } from 'vue'
import clipImg from '@/components/clipImg.vue'
import songListComponent from '@/components/songList.vue'
import { message } from '@/utils/message.js'

const route = useRoute()
const router = useRouter()
const songListStore = useSongListStore()
const isEditing = ref(false)
const clipImgRef = ref(null)
const coverImgRef = ref(null)
const infoRef = ref(null)
const songList = computed(() => {
	return songListStore.getListById(route.params.id)
})
const formData = reactive({
	id: songList.value.id,
	name: songList.value.name,
	coverImg: songList.value.coverImg,
})

watch(
	() => songList.value,
	newVal => {
		if (newVal) {
			formData.id = newVal.id
			formData.name = newVal.name
			formData.coverImg = newVal.coverImg
			isEditing.value = false
		}
	},
	{ immediate: true }
)
const showClip = () => {
	if (!isEditing.value) return
	clipImgRef.value.showClipImg = true
}

const submitImg = async (image, blob) => {
	const fd = new FormData()
	fd.append('coverImg', blob)
	const data = await songListStore.uploadListCoverImg(fd)
	formData.coverImg = data
	return true
}

const delList = async listId => {
	const res = await songListStore.deleteSongList(listId)
	if (res) {
		message('删除成功', 'success')
		router.go(-1)
	}
}

const cancelUpdate = e => {
	if (!isEditing.value) return
	if (
		!coverImgRef.value.contains(e.target) &&
		!infoRef.value.contains(e.target)
	) {
		isEditing.value = false
		rollback()
	}
}

const updateList = async () => {
	if (!validate()) return
	await songListStore.updateList(formData)
	isEditing.value = false
	formData.coverImg = songList.value.coverImg
	message('更新成功', 'success')
}
const validate = () => {
	if (formData.name.length < 3) {
		message('歌名不合法', 'warning')
		return false
	}
	return true
}

const rollback = () => {
	formData.id = songList.value.id
	formData.name = songList.value.name
	formData.coverImg = songList.value.coverImg
}

onMounted(() => {
	document.addEventListener('click', cancelUpdate)
})
onUnmounted(() => {
	document.removeEventListener('click', cancelUpdate)
})
</script>
<template>
	<clipImg ref="clipImgRef" :submit="submitImg"></clipImg>
	<div class="mySongList_container">
		<div class="header">
			<img
				ref="coverImgRef"
				:src="formData.coverImg"
				@click="showClip"
				:class="{ editImg: isEditing }"
			/>
			<div class="info" ref="infoRef">
				<i class="iconfont icon-top" v-show="isEditing" @click="updateList"></i>
				<input
					class="name"
					type="text"
					v-model="formData.name"
					:disabled="!isEditing"
				/>
				<div class="function">
					<button type="button">播放</button>
					<button type="button">下载</button>
					<button type="button" @click="delList(songList.id)">删除</button>
					<button type="button" @click="isEditing = true">编辑</button>
				</div>
			</div>
		</div>
		<div class="songList">
			<songListComponent :listData="songList"></songListComponent>
		</div>
	</div>
</template>
<style scoped>
.mySongList_container {
	display: flex;
	flex-direction: column;
	width: 100%;
	height: 100%;
	min-height: 0;
	box-sizing: border-box;
	padding: var(--main-padding);
	padding-top: 0;
	border-radius: 0 0 var(--main-border-radius) var(--main-border-radius);
}
.songList {
	flex: 1;
	width: 100%;
	min-height: 0;
	border-radius: 5px;
}
.header {
	display: flex;
	height: 150px;
	box-sizing: border-box;
}
.header img {
	height: 150px;
	width: 150px;
	margin-right: 30px;
	border-radius: 5px;
	box-sizing: border-box;
}
.header .editImg {
	border: 1px dashed skyblue;
	cursor: pointer;
}
.info {
	position: relative;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}
.info .name {
	font-size: 25px;
	background-color: transparent;
	border: none;
	outline: none;
}
.icon-top {
	position: absolute;
	right: 15px;
	top: 15px;
	font-size: 25px;
	color: rgb(150, 150, 150);
	cursor: pointer;
}
.icon-top:hover {
	color: skyblue;
}
.name:disabled {
	color: black;
}
.function {
	display: flex;
	width: 400px;
	justify-content: space-between;
}
.function button {
	width: 80px;
	height: 35px;
	border-radius: 17px;
	border: none;
	cursor: pointer;
	background-color: rgb(230, 230, 230);
}
.function button:hover {
	background-color: rgb(200, 200, 200);
}
</style>
