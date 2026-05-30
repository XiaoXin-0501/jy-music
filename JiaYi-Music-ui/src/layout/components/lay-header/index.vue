<script setup>
import { ref } from 'vue'
import Login from '@/views/login/index.vue'
import { useUserStore } from '@/store/userStore.js'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const loginRef = ref(null)
const isFocus = ref(false)
const handleAvatarClick = () => {
	if (userStore.isLogin) {
		loginRef.value.showLogin = false
		router.push('/myInfo')
	} else {
		loginRef.value.showLogin = true
	}
}
</script>
<template>
	<Login ref="loginRef"></Login>
	<div class="lay_header">
		<div class="header_search">
			<input
				type="text"
				name="searchData"
				maxlength="50"
				@focus="isFocus = true"
				@blur="isFocus = false"
			/>
			<i class="iconfont icon-sousuo" v-show="isFocus"></i>
		</div>
		<div class="header_avatar">
			<img :src="userStore.avatar" alt="" @click="handleAvatarClick" />
		</div>
	</div>
</template>
<style scoped>
.lay_header {
	display: flex;
	justify-content: space-between;
	flex: 0 0 40px;
	padding: var(--main-padding);
	padding-bottom: 0;
	border-radius: var(--main-border-radius) var(--main-border-radius) 0 0;
	background: var(--bc-header);
}
.header_search {
	position: relative;
	display: flex;
	width: 30%;
	height: 30px;
}
.header_search input {
	box-sizing: border-box;
	width: 100%;
	height: 30px;
	background: no-repeat var(--bc-header-search)
		url('../../../assets/png/search.png');
	background-size: 20px 20px;
	background-position: 5px center;
	border-radius: 10px;
	border: 2px solid var(--bc-header-search-border);
	outline: none;
}
.header_search input:focus {
	background-image: none;
}
.header_search .icon-sousuo {
	position: absolute;
	top: 50%;
	right: 5px;
	font-size: 20px;
	transform: translateY(-50%);
	color: rgb(214, 223, 237);
	cursor: pointer;
}
.header_avatar {
	width: 30px;
	height: 30px;
	margin-right: 20px;
}
.header_avatar img {
	width: 100%;
	height: 100%;
	border-radius: 50%;
	box-shadow: 0 0 3px 2px var(--bc-header-login-shadow);
	cursor: pointer;
}
</style>
