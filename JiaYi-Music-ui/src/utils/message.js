import { ElMessage } from 'element-plus'

//params按需添加
const message = (content, type = 'info', placement = 'top', params = {}) => {
	const {
		duration = 3000,
		showClose = true,
		grouping = true,
		...other
	} = params
	ElMessage({
		message: content,
		placement,
		type,
		duration,
		showClose,
		grouping,
		...other,
	})
}
export { message }
