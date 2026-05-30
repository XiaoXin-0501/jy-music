<script setup>
import { computed, reactive, ref } from 'vue'
import { handleString } from '../utils/string'
import { FileUploader, getSuffix } from '@/utils/fileUtil/index.js'

const props = defineProps({
	api: Object,
	type: Array,
})
const emit = defineEmits(['handleRes'])
let fileUploader = null
const uploadState = reactive({
	progress: 0,
	isPaused: false,
	isUploaded: false,
	isCanceled: false,
})
const inputRef = ref(null)
const file = ref(null)
const showFileName = computed(() => {
	if (!file.value) return null
	// 完整文件名
	const fullName = file.value.name
	// 找到最后一个 . 的位置
	const lastDotIndex = fullName.lastIndexOf('.')
	// 没有后缀的情况
	if (lastDotIndex === -1) {
		const res = handleString(fullName, 15)
		if (res[0] > 15) {
			return res[1] + '...'
		} else {
			return fullName
		}
	} else {
		const prefix = fullName.slice(0, lastDotIndex)
		const suffix = fullName.slice(lastDotIndex + 1)
		const res = handleString(prefix, 12)
		if (res[0] > 12) {
			return res[1] + '...' + suffix
		} else {
			return fullName
		}
	}
})
const selectFile = () => {
	inputRef.value.click()
}
const handleFileChange = async e => {
	initUploadState()
	file.value = e.target.files[0]
	e.target.value = null
	fileUploader = new FileUploader(file.value, uploadState, props.api)
	const res = await fileUploader.uploadFile()
	emit('handleRes', res, file.value)
}

const handleCancel = async () => {
	await fileUploader.cancel()
	file.value = null
}
const initUploadState = () => {
	uploadState.isCanceled = false
	uploadState.isPaused = false
	uploadState.isUploaded = false
	uploadState.progress = 0
	file.value = null
}
const updateFile = url => {
	if (url) uploadState.isUploaded = true
}
defineExpose({ initUploadState, updateFile })
</script>
<template>
	<div class="fileInput_container">
		<div class="fileInput_main">
			<slot name="label">文件：</slot>
			<button
				@click="selectFile"
				type="button"
				:class="{ uploadSuccess: uploadState.isUploaded }"
			>
				{{ showFileName || '选择要上传的文件' }}
			</button>
			<input
				ref="inputRef"
				type="file"
				:accept="(props.type || []).join(',')"
				@change="handleFileChange"
				style="display: none"
			/>
		</div>
		<div class="progressBar" v-show="file && !uploadState.isUploaded">
			<i
				class="iconfont icon-play"
				@click="fileUploader.resume"
				v-if="uploadState.isPaused"
			></i>
			<i class="iconfont icon-pause" @click="fileUploader.pause" v-else></i>
			<div
				class="bar"
				:style="{ '--percent': uploadState.progress + '%' }"
				:data-percent="uploadState.progress + '%'"
			>
				<div class="progress"></div>
			</div>
			<i class="iconfont icon-guanbi" @click="handleCancel"></i>
		</div>
	</div>
</template>
<style scoped>
.fileInput_container {
	width: 250px;
	height: 50px;
	display: flex;
	flex-direction: column;
	align-items: end;
}
.fileInput_main button {
	width: 200px;
	border: 1px solid rgba(150, 150, 150, 0.5);
	border-radius: 3px;
	padding: 5px;
	font-size: 12px;
	cursor: pointer;
	background-color: transparent;
}
.fileInput_main button:hover {
	background-color: rgba(230, 230, 230, 0.5);
}
button.uploadSuccess {
	background-color: rgba(139, 241, 76, 0.5);
}
.progressBar {
	display: flex;
	align-items: center;
	margin-top: 5px;
}
.progressBar .iconfont {
	margin: 0 10px;
	cursor: pointer;
	color: rgb(150, 150, 150);
}
.progressBar .icon-guanbi {
	margin: 0 0 0 5px;
}
.icon-guanbi:hover {
	color: red;
}
.bar {
	position: relative;
	width: 175px;
	height: 5px;
	border: 1px solid rgb(200, 200, 200);
	border-radius: 3px;
}
.bar::before {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	font-size: 10px;
	content: attr(data-percent);
}
.progress {
	width: var(--percent);
	height: 100%;
	border-radius: 3px;
	background-color: rgb(8, 217, 241);
}
</style>
