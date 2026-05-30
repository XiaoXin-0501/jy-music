import { http } from '@/api/http/index.js'

export const captchaApi = () => {
	return http.get('/captcha')
}
