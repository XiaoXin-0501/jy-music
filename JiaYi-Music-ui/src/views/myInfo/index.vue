<script setup>
import { useUserStore } from '@/store/userStore.js'
import { storeToRefs } from 'pinia'
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import clipImg from '@/components/clipImg.vue'

const userStore = useUserStore()
const { avatar, nickName, email, sex, birthday } = storeToRefs(userStore)
const nickNameRef = ref(null)
const updateAvatarRef = ref(null)
const formRef = ref(null)
const editing = ref(false)
const sexData = ['女', '男', '未知']
const formData = ref({
	nickName: nickName.value,
	birthday: birthday.value,
	email: email.value,
	sex: sex.value,
})

const startEditor = async () => {
	editing.value = true
	await nextTick() // 等待 DOM 更新完成
	nickNameRef.value.focus()
}

const handleClick = e => {
	if (!editing.value || e.target.closest('.arco-picker-container')) return
	if (formRef.value && !formRef.value.contains(e.target)) {
		editing.value = false
		rollBack()
	}
}

const submitInfo = () => {
	if (!validateForm()) return
	const res = userStore.updateUserInfo(formData.value)
	if (res) {
		editing.value = false
	} else {
		rollBack()
	}
}

const submitAvatar = (image, blob) => {
	const res = userStore.updateAvatar(blob)
	if (res) {
		return true
	}
}

const validateForm = () => {
	return true
}

const rollBack = () => {
	formData.value.nickName = nickName.value
	formData.value.sex = sex.value
	formData.value.email = email.value
	formData.value.birthday = birthday.value
}

onMounted(() => {
	document.addEventListener('click', handleClick)
})
onUnmounted(() => {
	document.removeEventListener('click', handleClick)
})
</script>
<template>
	<clipImg ref="updateAvatarRef" :submit="submitAvatar" type="round"></clipImg>
	<div class="myInfo_container">
		<div class="info">
			<div class="avatar">
				<img :src="avatar" alt="" />
				<i
					class="iconfont icon-shangchuan_huaban"
					@click="updateAvatarRef.showClipImg = true"
				></i>
			</div>
			<div class="otherData">
				<form class="data" ref="formRef">
					<i class="iconfont icon-bianji" title="编辑" @click="startEditor"></i>
					<input
						class="nickName"
						ref="nickNameRef"
						type="text"
						maxlength="5"
						v-model="formData.nickName"
						:disabled="!editing"
					/>
					<div class="sexInput" v-if="editing">
						<label>
							性别：男 <input type="radio" v-model="formData.sex" value="1" />
						</label>
						<label
							>女 <input type="radio" v-model="formData.sex" value="0"
						/></label>
						<label
							>未知 <input type="radio" v-model="formData.sex" value="2"
						/></label>
					</div>
					<span v-else>性别：{{ sexData[formData.sex] }}</span>
					<label
						>生日：
						<a-date-picker v-model="formData.birthday">
							<a-button
								class="birthdayInput"
								type="text"
								:disabled="!editing"
								>{{ formData.birthday || '请选择日期' }}</a-button
							>
						</a-date-picker>
					</label>
					<label class="emailLabel"
						>email：<input
							type="email"
							v-model="formData.email"
							:disabled="!editing"
					/></label>
					<i
						class="iconfont icon-top"
						title="提交"
						v-if="editing"
						@click="submitInfo"
					></i>
				</form>
			</div>
		</div>
	</div>
</template>
<style scoped>
.myInfo_container {
	width: 100%;
	height: 100%;
	box-sizing: border-box;
	padding: var(--main-padding);
	padding-top: 0;
	border-radius: 0 0 var(--main-border-radius) var(--main-border-radius);
}
.info {
	display: flex;
}
.info .avatar {
	position: relative;
}
.info .avatar,
.avatar img {
	width: 150px;
	height: 150px;
	border-radius: 50%;
}
.icon-shangchuan_huaban {
	position: absolute;
	bottom: 3px;
	right: 3px;
	font-size: 30px;
	color: rgba(180, 180, 180, 0.5);
	cursor: pointer;
}
.icon-shangchuan_huaban:hover {
	color: rgb(129, 234, 238);
}
.info .otherData {
	margin-left: 100px;
}
.data {
	position: relative;
	display: flex;
	flex-direction: column;
}
.data input {
	border: none;
	background-color: transparent;
	padding: 0;
	width: 200px;
}
.data input:focus {
	outline: none;
	border-bottom: 1px solid rgba(100, 100, 100, 0.5);
}
.data .nickName {
	font-size: 30px;
	font-weight: 1000;
	margin-bottom: 20px;
}
.sexInput label {
	margin-right: 10px;
}
.sexInput label input {
	width: 10px;
}
.birthdayInput {
	margin: 5px 0;
	padding: 0;
	color: black;
}
.arco-btn-disabled {
	color: inherit !important;
	opacity: 1 !important;
	cursor: default !important;
	user-select: text !important;
}
.icon-bianji,
.icon-top {
	position: absolute;
	font-size: 18px;
	cursor: pointer;
	color: rgb(200, 200, 200);
}
.icon-bianji {
	top: 1px;
	right: 1px;
}
.icon-top {
	bottom: 1px;
	right: 1px;
}
.icon-bianji:hover,
.icon-top:hover {
	color: rgb(129, 234, 238);
}
</style>
