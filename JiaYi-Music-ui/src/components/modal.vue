<script setup>
import { ref } from 'vue'

const propos = defineProps({
	confirm: Function,
	cancel: Function,
})

const show = ref(false)
const handleCancel = () => {
	if (!propos.cancel) {
		show.value = false
		return
	}
	propos.cancel()
}
defineExpose({ show })
</script>
<template>
	<Teleport to="#app">
		<div class="modal" v-if="show">
			<div class="main">
				<div class="title">
					<slot></slot>
				</div>
				<button class="confirm" type="button" @click="handleCancel">
					<slot name="confirmText">取消</slot>
				</button>
				<button class="cancel" type="button" @click="confirm">
					<slot name="cancelText">确定</slot>
				</button>
			</div>
		</div>
	</Teleport>
</template>
<style scoped>
.modal {
	display: flex;
	justify-content: center;
	align-items: center;
	width: 100%;
	height: 100%;
	position: absolute;
	top: 0;
	left: 0;
	z-index: var(--z-index-top);
	background-color: rgba(200, 200, 200, 0.3);
}
.main {
	display: grid;
	grid-template-columns: 1fr 1fr;
	grid-template-rows: repeat(5, 1fr);
	width: 300px;
	height: 200px;
	border: 1px solid rgb(150, 150, 150);
	border-radius: 5px;
	background-color: white;
}
.title {
	padding: 5px;
	/* 文字换行 */
	word-break: break-all;
	grid-row: 2/3;
	grid-column: span 2;
	text-align: center;
}
button {
	padding: 10px 15px;
	border-radius: 5px;
	border: 1px solid rgb(200, 200, 200, 0.5);
	cursor: pointer;
	place-self: center;
	grid-row: 4/5;
}
button:hover {
	color: white;
	background-color: rgb(114, 183, 255);
}
</style>
