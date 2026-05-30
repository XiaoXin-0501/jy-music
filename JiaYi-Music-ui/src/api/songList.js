import { http } from '@/api/http/index.js'

export const getCategortListApi = () => {
	return http.get('/song/category')
}
export const getSongListApi = () => {
	return http.get('/song/songList')
}

export const addSongToListApi = (listId, songId) => {
	return http.post(`/song/addSongToList/${songId}/${listId}`)
}

export const removeSongFromListApi = (songId, listId) => {
	return http.post(`/song/removeSongFromList/${songId}/${listId}`)
}
export const createSongListApi = name => {
	return http.post(`/song/createSongList/${name}`)
}

export const deleteSongListApi = listId => {
	return http.post(`/song/deleteSongList/${listId}`)
}

export const uploadListCoverImgApi = coverImg => {
	return http.post('/song/file/listCoverImg', coverImg)
}

export const updateListApi = formData => {
	return http.post('/song/updateSongList', formData)
}

export const getPlayRankApi = () => {
	return http.get('/song/getPlayRank')
}

export const getCollectRankApi = () => {
	return http.get('/song/getCollectRank')
}

export const getHotRankApi = () => {
	return http.get('/song/getHotRank')
}
