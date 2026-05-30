<script setup>
import { useUserStore } from '@/store/userStore.js'
import { reactive, ref, onBeforeMount, watch } from 'vue'
import { captchaApi } from '@/api/captcha.js'
//验证码占位图
import captchaPlaceHolder from '@/assets/png/update-captcha.png'

const formRef = ref(null)
const captchaUrl = ref('')
const containerRef = ref(null)
const showLogin = ref(false)

defineExpose({
	showLogin,
})

watch(showLogin, newData => {
	if (newData === true) {
		captchaUrl.value = captchaPlaceHolder
		updateCaptcha()
	} else {
		clearData()
	}
})

const userStore = useUserStore()
const loginBody = reactive({
	username: '',
	password: '',
	uuid: '',
	code: '',
})

const rule = {
	username: [
		{ required: true, message: '请输入用户名', trigger: 'blur' },
		{ min: 2, message: '用户名长度至少为2', trigger: 'blur' },
		{ max: 5, message: '用户名长度最多为5', trigger: 'blur' },
	],
	password: [
		{ required: true, message: '请输入密码', trigger: 'blur' },
		{ min: 5, message: '密码长度至少为5', trigger: 'blur' },
		{ max: 15, message: '密码长度最多为15', trigger: 'blur' },
	],
	code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

const closeLogin = e => {
	if (!showLogin.value) {
		return
	}
	const markX = e.clientX
	const markY = e.clientY
	const formX = containerRef.value.offsetLeft
	const formY = containerRef.value.offsetTop
	const formWidth = containerRef.value.offsetWidth
	const formHeight = containerRef.value.offsetHeight
	if (
		!(
			markX > formX &&
			markX < formX + formWidth &&
			markY > formY &&
			markY < formY + formHeight
		)
	) {
		showLogin.value = false
	}
}

const updateCaptcha = async () => {
	try {
		const res = await captchaApi()
		loginBody.uuid = res.data.uuid
		captchaUrl.value = res.data.img
	} catch (error) {
		captchaUrl.value = captchaPlaceHolder
	}
}

const submit = async () => {
	try {
		await formRef.value.validate()
		const res = await userStore.login(loginBody)
		if (res) {
			showLogin.value = false
		}
	} catch (error) {
		updateCaptcha()
	}
}

const clearData = () => {
	// 清空表单所有字段
	loginBody.username = ''
	loginBody.password = ''
	loginBody.code = ''
	loginBody.uuid = ''
}
</script>
<template>
	<Teleport to="#app">
		<div class="login_mark" v-if="showLogin" @click="closeLogin">
			<div class="login_container" ref="containerRef">
				<div class="login_logo">
					<i class="iconfont icon-guanbi" @click="showLogin = false"></i>
					<div class="logo_msg">
						<img src="@/assets/svg/jy-logo.svg" alt="加一音悦" />
					</div>
					<p>加一音悦</p>
				</div>
				<div class="login_form">
					<el-form
						@keyup.enter="submit"
						:model="loginBody"
						:rules="rule"
						size="large"
						ref="formRef"
						scroll-to-error
					>
						<el-form-item prop="username">
							<el-input
								v-model="loginBody.username"
								style="width: 300px"
								placeholder="用户名"
							/>
						</el-form-item>
						<el-form-item prop="password">
							<el-input
								v-model="loginBody.password"
								type="password"
								show-password
								style="width: 300px"
								placeholder="密码"
							/>
						</el-form-item>
						<el-form-item prop="code">
							<el-input
								v-model="loginBody.code"
								style="width: 100px"
								placeholder="验证码"
							/>
							<img
								:src="captchaUrl"
								alt=""
								class="captchaImg"
								@click="updateCaptcha"
							/>
						</el-form-item>
						<el-form-item>
							<el-button
								type="primary"
								round
								style="width: 300px"
								@click="submit"
							>
								登录
							</el-button>
						</el-form-item>
					</el-form>
				</div>
			</div>
		</div>
	</Teleport>
</template>
<style scoped>
.login_mark {
	position: fixed;
	top: 0;
	left: 0;
	z-index: var(--z-index-top);
	display: flex;
	justify-content: center;
	align-items: center;
	width: 100%;
	height: 100%;
}
.login_container {
	width: 450px;
	height: 550px;
	border-radius: 10px;
	box-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
	background-color: white;
}
.icon-guanbi {
	position: absolute;
	top: 5px;
	right: 5px;
	font-size: 25px;
	color: rgb(185, 185, 185);
	cursor: pointer;
}
.icon-guanbi:hover {
	color: rgb(230, 230, 230);
}
.login_logo {
	position: relative;
	width: 100%;
	height: 180px;
	border-radius: 10px 10px 1px 1px;
	background-color: rgba(20, 20, 20, 0.8);
}
.login_logo::after {
	position: absolute;
	left: 0;
	top: 180px;
	height: 10px;
	width: 100%;
	background: linear-gradient(to bottom, rgba(20, 20, 20, 0.8), transparent);
	content: '';
}
.logo_msg {
	display: flex;
	width: 100%;
	height: 100px;
	flex-direction: column-reverse;
	align-items: center;
}
.login_logo img {
	width: 80px;
	height: 80px;
}
.login_logo p {
	display: flex;
	justify-content: center;
	margin: 10px 0;
	color: white;
	font-size: 20px;
}
.login_form {
	width: 100%;
	height: 330px;
	display: flex;
	justify-content: center;
	align-items: center;
}
.el-form {
	display: flex;
	flex-direction: column;
	align-items: center;
}
.el-input {
	font-size: 18px;
}
.captchaImg {
	margin-left: 60px;
	cursor: pointer;
}
</style>
