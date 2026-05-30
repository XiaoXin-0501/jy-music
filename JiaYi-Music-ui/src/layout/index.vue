<script setup>
import Lay_main from './components/lay-main/index.vue'
import Lay_sidebar from './components/lay-sidebar/index.vue'
</script>
<template>
	<div class="layout_container">
		<Lay_sidebar></Lay_sidebar>
		<Lay_main></Lay_main>
	</div>
</template>
<style scoped>
.layout_container {
	display: flex;
	position: relative;
	width: 100%;
	height: 100%;
	background: var(--bc-layout);
}
</style>
