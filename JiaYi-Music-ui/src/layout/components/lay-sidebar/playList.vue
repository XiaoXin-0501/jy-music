<script setup>
import { useSongListStore } from '@/store/songListStore'
import { useUserStore } from '@/store/userStore'
import { storeToRefs } from 'pinia'
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import defaultCoverImg from '@/assets/png/defaultCoverImg.png'

const props = defineProps({
	showAll: Boolean,
})
const userStore = useUserStore()
const songListStore = useSongListStore()
const { otherList } = storeToRefs(songListStore)
const { isLogin } = storeToRefs(userStore)
const nameInputRef = ref(null)
const newListRef = ref(null)
const createListRef = ref(null)
const showNewList = ref(false)
const newName = ref('新建歌单' + (otherList.value.size + 1))

const createNewList = async () => {
	if (showNewList.value) return
	showNewList.value = true
	await nextTick()
	nameInputRef.value.focus()
}

const create = async e => {
	if (!showNewList.value) return
	if (
		newListRef.value &&
		!newListRef.value.contains(e.target) &&
		!createListRef.value.contains(e.target)
	) {
		await songListStore.createSongList(newName.value)
		showNewList.value = false
		newName.value = '新建歌单' + (otherList.value.size + 1)
	}
}
onMounted(() => {
	document.addEventListener('click', create)
})
onUnmounted(() => {
	document.removeEventListener('click', create)
})
</script>
<template>
	<div class="playList_container" v-if="showAll && isLogin">
		<div class="title"><p>我的歌单</p></div>
		<div class="createList" ref="createListRef" @click="createNewList">
			<i class="iconfont icon-jiahao"></i>
		</div>
		<ul class="list">
			<li class="newList" ref="newListRef" v-show="showNewList">
				<img class="coverImg" :src="defaultCoverImg" />
				<input
					ref="nameInputRef"
					type="text"
					maxlength="10"
					v-model="newName"
				/>
			</li>
			<li v-for="item in [...otherList.values()]">
				<router-link :to="{ name: 'MySongList', params: { id: item.id } }">
					<img class="coverImg" :src="item.coverImg" alt="" />
					<p>{{ item.name }}</p>
				</router-link>
			</li>
		</ul>
	</div>
	<div class="simpleContainer" v-if="!showAll">
		<ul class="simpleList">
			<li class="simpleItem" v-for="item in [...otherList.values()]">
				<router-link :to="{ name: 'MySongList', params: { id: item.id } }">
					<img class="simpleImg" :src="item.coverImg" alt="" />
				</router-link>
			</li>
		</ul>
	</div>
</template>
<style scoped>
.playList_container {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 100%;
}
.title {
	display: flex;
	height: 55px;
	justify-content: center;
	align-items: center;
}
.createList {
	display: flex;
	justify-content: center;
	align-items: center;
	width: 200px;
	height: 30px;
	border-radius: 5px;
	background-color: rgb(250, 250, 250);
	cursor: pointer;
}
.createList:hover {
	background-color: white;
}
.list {
	height: 250px;
	width: 200px;
	overflow-y: auto;
	scrollbar-width: none;
}
/*隐藏滚动条 */
.list::-webkit-scrollbar {
	display: none;
}
.list li {
	width: 100%;
	height: 55px;
	margin: 5px 0;
	border-radius: 5px;
	box-sizing: border-box;
}
li a {
	width: 100%;
	height: 100%;
	display: flex;
	padding: 5px;
	border-radius: 5px;
	box-sizing: border-box;
	background-color: rgb(250, 250, 250);
}
li a:hover {
	background-color: rgb(230, 230, 230);
}
.router-link-active {
	background-color: rgb(200, 200, 200);
}
li .coverImg {
	width: 45px;
	height: 45px;
	border-radius: 5px;
}
li p {
	margin-left: 15px;
}
.newList {
	display: flex;
	padding: 5px;
	border-radius: 5px;
	box-sizing: border-box;
	background-color: rgb(250, 250, 250);
}
.newList input {
	width: 130px;
	height: 40px;
	border: none;
}
.newList input:focus {
	outline: none;
}
.simpleList {
	height: 250px;
	overflow-y: auto;
	scrollbar-width: none;
}
.simpleItem {
	width: 55px;
	height: 55px;
	margin: 5px 0;
	border-radius: 5px;
	box-sizing: border-box;
}
</style>
