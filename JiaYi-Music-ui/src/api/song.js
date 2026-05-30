import { http } from '@/api/http/index.js'

export const getCategortListApi = () => {
	return http.get('/song/category')
}

export const uploadCoverImgApi = coverImg => {
	return http.post('/song/file/coverImg', coverImg)
}

export const mergeChunksApi = filename => {
	return http.post('/song/file/mergeChunks', null, {
		params: { filename },
	})
}

export const mergeLyricsChunksApi = filename => {
	return http.post('/song/file/mergeLyricsChunks', null, {
		params: { filename },
	})
}

export const isUploadApi = filename => {
	return http.get('/song/file/isUpload', {
		params: { filename },
	})
}
export const lyricsIsUploadApi = filename => {
	return http.get('/song/file/lyricsIsUpload', {
		params: { filename },
	})
}

export const uploadSongApi = formData => {
	return http.post('/song/upload', formData)
}

export const updateSongApi = (songId, formData) => {
	return http.put(`/song/update/${songId}`, formData)
}

export const deleteSongApi = songId => {
	return http.delete(`/song/delete/${songId}`)
}

export const getSongsApi = () => {
	return http.get('/song/getSongs')
}

export const downloadSongApi = songId => {
	return http.get(`/song/download/${songId}`)
}

export const getSongFileInfo = songId => {
	return http.head(`/song/download/${songId}`)
}

export const updatePlayCountApi = songId => {
	return http.put(`/song/updatePlayCount/${songId}`)
}

export const getRankSongsApi = () => {
	return http.get('/song/getRankSongs')
}
