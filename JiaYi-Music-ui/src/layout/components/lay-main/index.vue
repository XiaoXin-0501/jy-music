<script setup>
import { useCacheStore } from '@/store/cacheStore.js'
import { storeToRefs } from 'pinia'
import Lay_header from '../lay-header/index.vue'
import Lay_footer from '../lay-footer/index.vue'

const cacheStore = useCacheStore()
const { cacheList } = storeToRefs(cacheStore)
</script>
<template>
	<div class="layout_main">
		<Lay_header></Lay_header>
		<div class="main_body">
			<router-view v-slot="{ Component }">
				<keep-alive :include="cacheList">
					<component :is="Component" />
				</keep-alive>
			</router-view>
		</div>
		<lay_footer></lay_footer>
	</div>
</template>
<style scoped>
.layout_main {
	display: flex;
	flex-direction: column;
	min-width: 900px;
	min-height: 600px;
	flex-grow: 1;
	height: 100%;
	padding: 5px;
	box-sizing: border-box;
	background-color: var(--bc-main);
}
.main_body {
	min-height: 0;
	flex-grow: 1;
	background-color: var(--bc-main-body);
	border-radius: 0 0 5px 5px;
}
</style>
