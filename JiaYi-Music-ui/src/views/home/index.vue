<script setup>
import { computed, onUnmounted } from 'vue'
import { useSongListStore } from '@/store/songListStore.js'
import { useSongStore } from '@/store/songStore'
import { usePlayListStore } from '@/store/playListStore.js'
import { storeToRefs } from 'pinia'

const songListStore = useSongListStore()
const songStore = useSongStore()
const playListStore = usePlayListStore()
const { isPlaying } = storeToRefs(playListStore)
const { playRank, collectRank, hotRank } = storeToRefs(songListStore)

// 播放排行 - 只保留歌曲，按分数降序，6个一组
const playRankSongs = computed(() => {
	const list = Array.from(playRank.value?.entries?.() || [])
		.map(([id, score]) => ({
			song: songStore.getSongById(id),
			score: score,
		}))
		.filter(item => Boolean(item.song)) // 过滤无效歌曲
		.sort((a, b) => b.score - a.score) // 按分数从大到小排序
		.map(item => item.song)

	// 6个一组分组
	const groupSize = 6
	const groupedList = []
	for (let i = 0; i < list.length; i += groupSize) {
		groupedList.push(list.slice(i, i + groupSize))
	}

	return groupedList
})

// 收藏排行 - 只保留歌曲，按分数降序，6个一组
const collectRankSongs = computed(() => {
	const list = Array.from(collectRank.value?.entries?.() || [])
		.map(([id, score]) => ({
			song: songStore.getSongById(id),
			score: score,
		}))
		.filter(item => Boolean(item.song)) // 过滤无效歌曲
		.sort((a, b) => b.score - a.score) // 按分数从大到小排序
		.map(item => item.song)

	// 6个一组分组
	const groupSize = 6
	const groupedList = []
	for (let i = 0; i < list.length; i += groupSize) {
		groupedList.push(list.slice(i, i + groupSize))
	}

	return groupedList
})

const hotRankSongs = computed(() => {
	return new Map(
		Array.from(hotRank.value?.entries() || [])
			.map(([id, score]) => [songStore.getSongById(id), score])
			.filter(([song]) => Boolean(song))
	)
})

const handlePlay = (song, list) => {
	if (song.id === playListStore.currentSong?.id) {
		if (isPlaying.value) {
			playListStore.pause()
		} else {
			playListStore.resume()
		}
		return
	}
	playListStore.playSong(song, list)
}

let timer = setInterval(() => {
	;(songListStore.getPlayRank(),
		songListStore.getCollectRank(),
		songListStore.getHotRank(),
		songStore.getRankSongs(),
		playListStore.start())
}, 30 * 1000)

onUnmounted(() => {
	if (timer) {
		clearInterval(timer)
		timer = null
	}
})
</script>
<template>
	<div class="home_container">
		<div class="rank">
			<div class="rank_play">
				<div class="title">热歌榜</div>
				<el-carousel
					:interval="4000"
					type="card"
					height="200px"
					trigger="click"
				>
					<el-carousel-item
						v-for="[song, score] in hotRankSongs.entries()"
						:key="song.id"
					>
						<div class="info_song">
							<img class="hot_img" :src="song.coverImg" :alt="song.name" />
							<div
								class="hot_mark"
								@click="handlePlay(song, [...hotRankSongs.values()])"
							>
								<i
									class="iconfont icon-pause"
									v-if="isPlaying && playListStore.currentSong.id === song.id"
								></i>

								<i class="iconfont icon-play" v-else></i>
							</div>
							<div class="other">
								<div class="name">{{ song.name }}</div>
								<div class="score">热歌指数: {{ score }}</div>
							</div>
						</div>
					</el-carousel-item>
				</el-carousel>
			</div>
			<div class="rank_collect">
				<div class="title">收藏榜</div>
				<el-carousel
					:interval="5000"
					arrow="always"
					indicator-position="none"
					height="250px"
				>
					<el-carousel-item v-for="item in collectRankSongs" :key="item">
						<div class="gridSongs">
							<div class="gridItem" v-for="song in item" :key="song.id">
								<img :src="song.coverImg" :alt="song.name" />
								<div
									class="mark"
									@click="handlePlay(song, [...collectRank.keys()])"
								>
									<i
										class="iconfont icon-pause"
										v-if="isPlaying && playListStore.currentSong.id === song.id"
									></i>

									<i class="iconfont icon-play" v-else></i>
								</div>
								<div class="otherInfo">
									<div class="name">{{ song.name }}</div>
									<div class="singer">{{ song.singer }}</div>
								</div>
							</div>
						</div>
					</el-carousel-item>
				</el-carousel>
			</div>
			<div class="rank">
				<div class="title">播放榜</div>
				<el-carousel
					:interval="5000"
					arrow="always"
					indicator-position="none"
					height="250px"
				>
					<el-carousel-item v-for="item in playRankSongs" :key="item">
						<div class="gridSongs">
							<div class="gridItem" v-for="song in item" :key="song.id">
								<img :src="song.coverImg" :alt="song.name" />
								<div
									class="mark"
									@click="handlePlay(song, [...playRank.keys()])"
								>
									<i
										class="iconfont icon-pause"
										v-if="isPlaying && playListStore.currentSong.id === song.id"
									></i>

									<i class="iconfont icon-play" v-else></i>
								</div>
								<div class="otherInfo">
									<div class="name">{{ song.name }}</div>
									<div class="singer">{{ song.singer }}</div>
								</div>
							</div>
						</div>
					</el-carousel-item>
				</el-carousel>
			</div>
		</div>
	</div>
</template>
<style scoped>
.home_container {
	display: flex;
	flex-direction: column;
	width: 100%;
	height: 100%;
	min-height: 0;
	box-sizing: border-box;
	padding: var(--main-padding);
	padding-top: 0;
	border-radius: 0 0 var(--main-border-radius) var(--main-border-radius);
	overflow: auto;
}
.info_song {
	position: relative;
	display: flex;
	width: 100%;
	height: 100%;
}
.hot_img {
	width: 200px;
	border-radius: 5px;
}
.hot_mark {
	display: none;
	position: absolute;
	top: 0;
	left: 0;
	width: 200px;
	height: 200px;
	justify-content: center;
	align-items: center;
	border-radius: 5px;
	opacity: 0.5;
}
.info_song:hover .hot_mark {
	display: flex;
	background-color: rgba(0, 0, 0, 0.5);
	color: white;
}
.info_song .other {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	flex: 1;
	padding: 5px;
	padding-left: 10px;
	background-color: rgb(223, 238, 240);
}
.other .name {
	font-size: 20px;
	font-weight: bold;
	padding: 20px;
}
.other .score {
	padding: 20px;
}
.gridSongs {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	grid-template-rows: repeat(2, 1fr);
	gap: 10px;
	width: 100%;
	height: 100%;
}
.gridItem {
	position: relative;
	display: flex;
	padding: 10px;
	border-radius: 5px;
	background-color: rgb(245, 245, 245);
	cursor: pointer;
}
.mark {
	position: absolute;
	left: 10px;
	top: 10px;
	width: 100px;
	height: 100px;
	border-radius: 5px;
	display: none;
	justify-content: center;
	align-items: center;
	opacity: 0.5;
}
.gridItem:hover .mark {
	display: flex;
	background-color: rgba(0, 0, 0, 0.5);
	color: white;
}
.gridItem img {
	width: 100px;
	height: 100px;
	object-fit: cover;
	border-radius: 5px;
}
.gridItem .otherInfo {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	border-radius: 5px;
	padding: 5px;
	padding-left: 10px;
}
.title {
	font-size: 16px;
	margin: 20px 0;
}
.icon-play {
	font-size: 30px;
}
.icon-pause {
	font-size: 30px;
}
</style>
