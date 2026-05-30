import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '@/api/user.js'
import { useAppStore } from './appStore'
import {
	getUserInfoApi,
	updateAvatarApi,
	updateUserInfoApi,
	logoutApi,
} from '@/api/user.js'
import { message } from '@/utils/message.js'
import { user_common, user_vip, user_admin, visitor } from '@/router/enums.js'
import awatorPlaceHolder from '@/assets/png/login.png'
import { useRouter } from 'vue-router'

export const useUserStore = defineStore('userStore', () => {
	const appStore = useAppStore()

	const router = useRouter()
	const userId = ref()
	const roles = ref([visitor])
	const isLogin = ref(false)
	const nickName = ref('')
	const account = ref('')
	const avatar = ref(awatorPlaceHolder)
	const email = ref('')
	const sex = ref('')
	const birthday = ref('')
	const status = ref('')

	const login = async loginBody => {
		if (isLogin.value) return true
		const res = await loginApi(loginBody)
		if (res) {
			message('登录成功', 'success')
			localStorage.setItem('token', 'Bearer ' + res.data)
			isLogin.value = true
			await appStore.initAppByToken()
			return true
		}
		return false
	}

	const logout = async () => {
		if (!isLogin.value) return
		await logoutApi(userId.value)
		localStorage.removeItem('token')
		roles.value = [visitor]
		isLogin.value = false
		nickName.value = ''
		avatar.value = awatorPlaceHolder
		email.value = ''
		sex.value = ''
		status.value = ''
		birthday.value = ''
		account.value = ''
		message('成功退出', 'success')
		router.push('/home')
	}

	const getUserInfo = async () => {
		const res = await getUserInfoApi()
		isLogin.value = true
		const data = res.data
		if (data) {
			const backendRoles = data.roles.map(roleObj => mapRoleToKey(roleObj))
			userId.value = data.id
			roles.value = [...new Set([...backendRoles, ...roles.value, user_common])]
			nickName.value = data.nickName
			account.value = data.account
			avatar.value = data.avatar
			email.value = data.email
			sex.value = data.sex
			birthday.value = data.birthday
			status.value = data.status
		}
	}

	const updateAvatar = async blob => {
		const fd = new FormData()
		fd.append('avatarfile', blob)
		const res = await updateAvatarApi(fd)
		if (res.code === 2000) {
			avatar.value = res.data.avatar
			message('上传成功!', 'success')
		} else {
			message('上传失败', 'error')
		}
		return res.code === 2000
	}

	const updateUserInfo = async formData => {
		const res = await updateUserInfoApi(formData)
		if (res.code === 2000) {
			message('上传成功!', 'success')
			nickName.value = formData.nickName
			sex.value = formData.sex
			email.value = formData.email
			birthday.value = formData.birthday
		} else {
			message('上传失败', 'error')
		}
		return res.code === 2000
	}

	const mapRoleToKey = roleObj => {
		switch (roleObj.roleName) {
			case '管理员':
				return user_admin
			case 'vip用户':
				return user_vip
			case '普通用户':
				return user_common
			default:
				return visitor // 默认访客
		}
	}

	return {
		login,
		logout,
		getUserInfo,
		updateAvatar,
		updateUserInfo,
		account,
		roles,
		nickName,
		avatar,
		email,
		sex,
		status,
		birthday,
		isLogin,
	}
})
