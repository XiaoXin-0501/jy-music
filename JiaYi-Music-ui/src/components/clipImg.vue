<script setup>
import { ref, onMounted } from 'vue'
import { message } from '@/utils/message.js'

const props = defineProps({
	/**
	 * submit(image,blob)
	 */
	submit: Function,
	/**
	 * "round":圆形
	 * "square"：正方形
	 */
	type: String,
})
let ctx = null
const showClipImg = ref(false)
const inputRef = ref(null)
const canvasRef = ref(null)
const showUpload = ref(true)
const showCanvas = ref(null)
const preImg = {
	width: 0,
	height: 0,
	scale: 1,
	x: 0,
	y: 0,
	offsetX: 0,
	offsetY: 0,
	image: null,
}
const clipData = {
	width: 150,
	height: 150,
	x: 0,
	y: 0,
}
const moveData = {
	allowMove: false,
	startX: 0,
	startY: 0,
}

defineExpose({
	showClipImg,
})

const handleChange = e => {
	const file = e.target.files[0]
	const reader = new FileReader()
	reader.onload = () => {
		const img = new Image()
		img.onload = () => {
			preImg.width = img.width
			preImg.height = img.height
			preImg.image = img
			initPreImg()
			drawPreImg()
		}
		img.src = reader.result
		showUpload.value = false
		e.target.value = null
	}
	reader.readAsDataURL(file)
}

const handleWheel = e => {
	if (e.deltaY < 0) {
		zoomIn()
	} else {
		zoomOut()
	}
}

const drawPreImg = () => {
	ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
	ctx.drawImage(preImg.image, preImg.x, preImg.y, preImg.width, preImg.height)
	// 画出蒙层和裁剪区域
	ctx.fillStyle = 'rgba(100, 100, 100, 0.3)'
	ctx.fillRect(0, 0, ctx.canvas.width, clipData.y)
	ctx.fillRect(0, clipData.height + clipData.y, ctx.canvas.width, clipData.y)
	ctx.fillRect(0, clipData.y, clipData.x, clipData.height)
	ctx.fillRect(
		clipData.width + clipData.x,
		clipData.y,
		clipData.x,
		clipData.height
	)
	ctx.strokeStyle = 'rgba(200,200,200,0.5)'
	ctx.strokeRect(clipData.x, clipData.y, clipData.width, clipData.height)
	//画出裁剪后的图片
	const showCtx = showCanvas.value.getContext('2d')
	showCtx.clearRect(0, 0, showCtx.canvas.width, showCtx.canvas.height)
	showCtx.drawImage(
		ctx.canvas,
		clipData.x,
		clipData.y,
		clipData.width,
		clipData.height,
		0,
		0,
		clipData.width,
		clipData.height
	)
}

const zoomIn = () => {
	if (preImg.scale > 10) return
	preImg.scale *= 1.1
	preImg.width *= 1.1
	preImg.height *= 1.1
	updatePosition()
	drawPreImg()
}

const zoomOut = () => {
	if (preImg.scale < 0.25) return
	preImg.scale *= 0.9
	preImg.width *= 0.9
	preImg.height *= 0.9
	updatePosition()
	drawPreImg()
}

const startMove = e => {
	moveData.allowMove = true
	moveData.startX = e.offsetX
	moveData.startY = e.offsetY
}

const moveImg = e => {
	if (moveData.allowMove) {
		const endX = e.offsetX
		const endY = e.offsetY
		preImg.offsetX += endX - moveData.startX
		preImg.offsetY += endY - moveData.startY
		moveData.startX = endX
		moveData.startY = endY
		updatePosition()
		drawPreImg()
	}
}

const endMove = () => {
	moveData.allowMove = false
}

const initPreImg = () => {
	const ctxWidth = ctx.canvas.width
	const ctxHeight = ctx.canvas.height
	if (preImg.width > preImg.height) {
		if (preImg.width > ctxWidth) {
			preImg.height = (ctxWidth / preImg.width) * preImg.height
			preImg.width = ctxWidth
		}
	} else {
		if (preImg.height > ctxHeight) {
			preImg.width = (ctxHeight / preImg.height) * preImg.width
			preImg.height = ctxHeight
		}
	}
	preImg.scale = 1
	preImg.offsetX = 0
	preImg.offsetY = 0
	updatePosition()
}

const updatePosition = () => {
	const ctxWidth = ctx.canvas.width
	const ctxHeight = ctx.canvas.height
	preImg.x = (ctxWidth - preImg.width) / 2 + preImg.offsetX
	preImg.y = (ctxHeight - preImg.height) / 2 + preImg.offsetY
}

const initData = () => {
	ctx = canvasRef.value.getContext('2d')
	clipData.x = (ctx.canvas.width - clipData.width) / 2
	clipData.y = (ctx.canvas.height - clipData.height) / 2
}

const handleSubmit = () => {
	showCanvas.value.toBlob(async blob => {
		if (preImg.image === null) {
			message('请选择图片', 'error')
			return
		}
		const res = await props.submit(preImg.image, blob)
		if (res) {
			showClipImg.value = false
			ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height)
			preImg.image = null
			showUpload.value = true
		} else {
			message('加载出错', 'error')
		}
	})
}

onMounted(() => {
	initData()
})
</script>
<template>
	<Teleport to="#app">
		<div
			class="clipImg_container"
			@mouseup="endMove"
			@click.stop
			v-show="showClipImg"
		>
			<div class="clipImg_main">
				<i class="iconfont icon-guanbi" @click="showClipImg = false"></i>
				<div class="clip_main">
					<div class="upload" v-show="showUpload">
						<input
							type="file"
							ref="inputRef"
							accept=".jpg,.png"
							@change="handleChange"
						/>
						<i class="iconfont icon-jiahao"></i>
					</div>
					<canvas
						v-show="!showUpload"
						ref="canvasRef"
						@mousedown="startMove"
						@mousemove="moveImg"
						@mouseup="endMove"
						@wheel="handleWheel"
						width="300"
						height="300"
					></canvas>
				</div>
				<div class="show_main">
					<canvas
						ref="showCanvas"
						width="150"
						height="150"
						:style="{ borderRadius: props.type === 'round' ? '50%' : '5%' }"
					></canvas>
				</div>
				<div class="clip_button">
					<el-button @click="inputRef.click()">上传</el-button>
					<i class="iconfont icon-fangda" @click="zoomIn"></i>
					<i class="iconfont icon-suoxiao" @click="zoomOut"></i>
				</div>
				<div class="show_button">
					<el-button type="success" plain @click="handleSubmit">
						<slot name="submitText">提交</slot>
					</el-button>
				</div>
			</div>
		</div>
	</Teleport>
</template>
<style scoped>
.clipImg_container {
	display: flex;
	justify-content: center;
	align-items: center;
	position: fixed;
	top: 0;
	left: 0;
	z-index: var(--z-index-top);
	width: 100%;
	height: 100%;
	background-color: rgba(100, 100, 100, 0.5);
}
.clipImg_main {
	position: relative;
	display: grid;
	grid-template-columns: 1fr 1fr;
	grid-template-rows: 75fr 25fr;
	width: 600px;
	height: 400px;
	background-color: white;
	border-radius: 5px;
}
.clipImg_main .icon-guanbi {
	position: absolute;
	top: 5px;
	right: 5px;
	font-size: 25px;
	color: rgb(185, 185, 185);
	cursor: pointer;
}
.clipImg_main .icon-guanbi:hover {
	color: rgb(230, 230, 230);
}
.clip_main .upload {
	display: flex;
	justify-content: center;
	align-items: center;
	width: 100%;
	height: 100%;
	border-radius: 5px;
	cursor: pointer;
	color: rgb(150, 150, 150);
}
.clip_main canvas {
	display: block;
}
.clip_main .upload:hover {
	background-color: rgb(236, 245, 255);
	color: rgb(114, 183, 255);
}
.show_main {
	display: flex;
	justify-content: center;
	align-items: center;
}
.show_main canvas {
	display: block;
	/* border-radius: 50%; */
	box-shadow: 0 0 10px rgba(180, 180, 180, 0.5);
}
.upload {
	position: relative;
}
.upload input {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	opacity: 0;
	cursor: pointer;
}
.upload .icon-jiahao {
	font-size: 100px;
}
.clip_button {
	display: flex;
	justify-content: space-evenly;
	align-items: center;
}
.clip_button .iconfont {
	cursor: pointer;
}
.clip_button .iconfont:hover {
	color: rgb(114, 183, 255);
}
.show_button {
	display: flex;
	justify-content: center;
	align-items: center;
}
canvas {
	display: block;
}
</style>
