<script setup>
import songList from '@/components/songList.vue'
import { useSongListStore } from '@/store/songListStore.js'
import { storeToRefs } from 'pinia'

const songListStore = useSongListStore()
const { favoriteList } = storeToRefs(songListStore)
</script>
<template>
	<div class="favorite_container">
		<h1>喜欢</h1>
		<div class="song_list">
			<songList :listData="favoriteList"></songList>
		</div>
	</div>
</template>
<style>
.favorite_container {
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
.song_list {
	flex: 1;
	width: 100%;
	min-height: 0;
	border-radius: 5px;
}
</style>
