<script setup>
import { usePlayListStore } from '@/store/playListStore.js'
import { useSongListStore } from '@/store/songListStore.js'
import { storeToRefs } from 'pinia'
import { message } from '@/utils/message'

const playListStore = usePlayListStore()
const songListStore = useSongListStore()
const { isPlaying, currentSong, progress } = storeToRefs(playListStore)
const { favoriteList } = storeToRefs(songListStore)

const changeProgress = e => {
	const width = e.target.clientWidth
	const offset = e.offsetX
	const percent = offset / width
	playListStore.seekTo(percent)
}
const handleFavorite = async () => {
	try {
		if (!currentSong.value) return
		if (!songListStore.isFavorite(currentSong.value.id)) {
			const toListId = favoriteList.value.id
			await songListStore.addSongtoList(toListId, currentSong.value.id)
			message('收藏成功', 'success')
		} else {
			await songListStore.removeSongFromList(
				currentSong.value.id,
				favoriteList.value.id
			)
			message('取消收藏成功', 'success')
		}
	} catch (e) {
		console.log(e)
	}
}
</script>
<template>
	<div class="lay_footer">
		<div class="footer_main">
			<div class="info">
				<img :src="currentSong?.coverImg" alt="" />
				<p class="songName">{{ currentSong?.name }}</p>
				<div class="otherFunc">
					<i
						class="iconfont icon-aixin"
						@click="handleFavorite"
						v-if="currentSong && songListStore.isFavorite(currentSong.id)"
					></i>
					<i class="iconfont icon-xihuan" @click="handleFavorite" v-else></i>
				</div>
			</div>
			<div class="play">
				<div class="playFunc">
					<div class="func_main">
						<i
							class="iconfont icon-shangyiqu101"
							@click="playListStore.playPre"
						></i>
						<div class="playOrPause">
							<i
								class="iconfont icon-play"
								@click="playListStore.resume"
								v-if="!isPlaying"
							></i>
							<i
								class="iconfont icon-pause"
								@click="playListStore.pause"
								v-else
							></i>
						</div>
						<i
							class="iconfont icon-xiayiqu101-copy"
							@click="playListStore.playNext"
						></i>
					</div>
				</div>
				<div class="progress" :style="{ '--percent': progress + '%' }">
					<div class="progress_main" @click="changeProgress">
						<div class="progress_body"></div>
					</div>
				</div>
			</div>
			<div class="other"></div>
		</div>
	</div>
</template>
<style scoped>
.lay_footer {
	height: 100px;
	flex: 0 0 100px;
	border-radius: 5px;
	margin-top: 10px;
	padding: 10px;
	box-sizing: border-box;
	background: var(--bc-footer);
}
.footer_main {
	display: grid;
	grid-template-columns: 250px 1fr 250px;
	width: 100%;
	height: 100%;
	border-radius: 5px;
}
.info {
	display: grid;
	grid-template-columns: 80px 1fr;
	grid-template-rows: 40px 40px;
}
.info img {
	width: 100%;
	height: 100%;
	border-radius: 5px;
	grid-row: span 2;
}
.songName {
	padding-left: 15px;
}
.info .otherFunc {
	display: flex;
	align-items: center;
	padding-left: 15px;
	cursor: pointer;
}
.icon-aixin {
	color: red;
}
.play {
	height: 100%;
	width: 100%;
	display: flex;
	flex-direction: column;
}
.playFunc,
.progress {
	display: flex;
	justify-content: center;
	align-items: center;
	height: 50%;
	width: 100%;
}
.func_main {
	display: flex;
	width: 300px;
	height: 100%;
	justify-content: space-evenly;
	align-items: center;
}
.func_main .iconfont {
	font-size: 25px;
	cursor: pointer;
}
.playOrPause {
	width: 50px;
	height: 100%;
	display: flex;
	justify-content: center;
	align-items: center;
	background-color: skyblue;
	border-radius: 25px;
	cursor: pointer;
}
.progress_main {
	width: 300px;
	height: 4px;
	border-radius: 2px;
	background-color: rgb(200, 200, 200);
	cursor: pointer;
}
.progress_body {
	width: var(--percent);
	height: 100%;
	border-radius: 2px;
	background-color: black;
}
</style>
