import { http } from '@/api/http/index.js'

export const loginApi = loginBody => {
	return http.post('/login', loginBody)
}

export const getUserInfoApi = () => {
	return http.get('/info')
}

export const updateAvatarApi = formdata => {
	return http.post('/user/avatar', formdata)
}

export const updateUserInfoApi = formdata => {
	return http.post('/user/updateUserInfo', formdata)
}

export const logoutApi = id => {
	return http.post(`/user/logout/${id}`)
}
