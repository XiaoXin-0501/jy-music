<script setup>
import { storeToRefs } from 'pinia'
import { useSongListStore } from '@/store/songListStore.js'
import { message } from '@/utils/message'

const songListStore = useSongListStore()
const emit = defineEmits(['favorite', 'delete', 'add'])
const propos = defineProps({
	song: Object,
	listId: String,
})
const { otherList } = storeToRefs(songListStore)
</script>
<template>
	<div class="functionList_container" @click.stop>
		<div class="item">
			<i class="iconfont icon-play"></i>
			<div class="title">播放</div>
		</div>
		<div class="item">
			<i class="iconfont icon-xiayishou_huaban"></i>
			<div class="title">下一首播放</div>
		</div>
		<div class="item" @click="emit('favorite', song)">
			<i
				class="iconfont icon-xihuan"
				v-if="!songListStore.isFavorite(song?.id)"
			></i>
			<i class="iconfont icon-aixin" v-else></i>
			<div class="title">我喜欢</div>
		</div>
		<div class="item add">
			<i class="iconfont icon-tianjiazengjiajia"></i>
			<div class="title">添加到</div>
			<ul class="addList" ref="addListRef">
				<li v-for="item in [...otherList.values()]" @click="emit('add', item)">
					<div class="name">{{ item.name }}</div>
				</li>
			</ul>
		</div>
		<div class="item">
			<i class="iconfont icon-xiazai"></i>
			<div class="title">下载</div>
		</div>
		<div class="item" @click="remove">
			<i class="iconfont icon-shanchu"></i>
			<div class="title" @click="emit('delete', song)">删除</div>
		</div>
	</div>
</template>
<style scoped>
.functionList_container {
	z-index: var(--z-index-top);
	width: 200px;
	border-radius: 5px;
	background-color: white;
	box-shadow: 0 0 10px 0 rgb(200, 200, 200);
}
.item {
	display: flex;
	border-radius: 5px;
	padding: 10px;
	cursor: pointer;
}
.item:hover {
	background-color: rgb(230, 230, 230);
}
.item .title {
	padding-left: 20px;
}
.item .iconfont {
	color: rgb(120, 120, 120);
}
.iconfont.icon-aixin {
	color: red;
}
.add {
	position: relative;
}
.add:hover .addList {
	display: block;
}
.addList {
	display: none;
	position: absolute;
	top: 10px;
	left: 200px;
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
