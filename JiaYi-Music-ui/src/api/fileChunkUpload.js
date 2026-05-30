import { http } from '@/api/http/index.js'
export const getChunksApi = fileHash => {
	return http.get('/song/file/getChunks', {
		params: { fileHash },
	})
}
export const uploadChunkApi = (formData, onUploadProgress, signal) => {
	return http.post('/song/file/uploadChunk', formData, {
		onUploadProgress,
		signal,
		timeout: 100000,
	})
}
export const cancelApi = fileHash => {
	return http.post('/song/file/cancel', null, {
		params: { fileHash },
	})
}
