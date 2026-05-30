<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/store/userStore.js'
import { storeToRefs } from 'pinia'
import modal from '@/components/modal.vue'
import playList from '@/layout/components/lay-sidebar/playList.vue'

const userStore = useUserStore()
const showTitle = ref(true)
const showList = ref(true)
const logoutRef = ref(null)
const { isLogin } = storeToRefs(userStore)
const handleLogout = () => {
	userStore.logout()
	logoutRef.value.show = false
}
const handleShowLogout = () => {
	if (!isLogin.value) return
	logoutRef.value.show = true
}

const menu = [
	{
		name: '首页',
		icon: 'icon-home-g',
		to: '/home',
	},
	{
		name: '喜欢',
		icon: 'icon-xihuan',
		to: '/favorite',
	},
	{
		name: '上传',
		icon: 'icon-shangchuan',
		to: '/inputSong',
	},
	{
		name: '下载',
		icon: 'icon-daochu',
		to: '/myDownload',
	},
]

const handleShow = () => {
	if (showTitle.value) {
		showTitle.value = false
		showList.value = false
	} else {
		showTitle.value = true
		setTimeout(() => {
			showList.value = true
		}, 350)
	}
}
</script>
<template>
	<modal ref="logoutRef" :confirm="handleLogout">您确定要退出吗？</modal>
	<div class="lay_sidebar" :style="{ flexBasis: showTitle ? '230px' : '55px' }">
		<div class="logo">
			<img src="@/assets/svg/jy-logo.svg" alt="加一音悦" />
			<div class="title" :class="{ close: !showTitle }">加一音悦</div>
		</div>
		<ul class="menu">
			<li v-for="item in menu">
				<router-link :to="item.to">
					<i :class="['iconfont', item.icon]"></i>
					<div class="title" :class="{ close: !showTitle }">
						{{ item.name }}
					</div>
				</router-link>
			</li>
		</ul>
		<playList :showAll="showList"></playList>
		<div class="footer">
			<i
				class="iconfont icon-tuichu"
				v-show="showTitle"
				@click="handleShowLogout"
			></i>
			<i
				class="iconfont icon-shouqi"
				@click="handleShow"
				:style="{ transform: showTitle ? 'rotate(0)' : 'rotate(180deg)' }"
			></i>
		</div>
	</div>
</template>
<style scoped>
.lay_sidebar {
	position: relative;
	flex: 0 0 230px;
	height: 100%;
	min-height: 700px;
	border-radius: 5px;
	transition: flex-basis 0.5s ease;
	background: var(--bc-sidebar);
}
.logo {
	display: flex;
	width: 100%;
	border-bottom: 1px dashed rgba(13, 227, 209, 0.8);
}
.logo img {
	padding: 15px;
	width: 25px;
	height: 25px;
}
.title {
	width: 160px;
	height: 100%;
	line-height: 55px;
	overflow: hidden;
	white-space: nowrap;
	transition: all 0.5s ease;
}
.menu .iconfont {
	padding: 15px;
	font-size: 25px;
	color: rgb(150, 150, 150);
}
.menu a {
	display: flex;
}
.menu a:hover {
	background-color: var(--bc-sidebar-item-hover);
}
.menu .router-link-active {
	background: var(--bc-sidebar-item-active);
	/* background: red; */
}
.footer {
	display: flex;
	justify-content: space-between;
	padding: 15px;
	box-sizing: border-box;
	align-items: center;
	position: absolute;
	left: 0;
	bottom: 0;
	width: 100%;
	height: 50px;
}
.icon-tuichu {
	font-size: 23px;
	cursor: pointer;
}
.icon-tuichu:hover {
	color: rgb(206, 45, 45);
}
.icon-shouqi {
	font-size: 25px;
	cursor: pointer;
	transition: all 0.5s ease;
}
.icon-shouqi:hover {
	color: aqua;
}
.close {
	opacity: 0;
	width: 0;
}
</style>
