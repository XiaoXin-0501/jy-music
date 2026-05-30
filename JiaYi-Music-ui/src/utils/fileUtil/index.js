import SparkMD5 from 'spark-md5'
//注意worker的引入，容易出错
import MyWorker from './worker.js?worker'

import { addController, removeController } from '@/api/http/index.js'

import {
	getChunksApi,
	uploadChunkApi,
	cancelApi,
} from '@/api/fileChunkUpload.js'
/**
 * worker池
 * size:worker数
 */

function getSuffix(file) {
	const fileName = file.name
	const ext = fileName.slice(((fileName.lastIndexOf('.') - 1) >>> 0) + 1)
	if (!ext) {
		throw new Error('文件格式有误')
	}
	return ext
}

function getAudioDuration(url) {
	return new Promise(resolve => {
		const audio = new Audio(url)
		audio.onloadedmetadata = () => {
			resolve(audio.duration)
		}
	})
}

class WorkerPool {
	constructor(size = navigator.hardwareConcurrency || 4) {
		this.size = size
		//worker池的worker
		this.workers = []
		//空闲的worker
		this.idleWorkers = []
		//等待队列
		this.queue = []
		//存储任务的回调，确保顺序.必要！否则同一个文件会得出不同的hash.
		this.callbacks = new Map()
		this.init()
	}

	init() {
		for (let i = 0; i < this.size; i++) {
			const worker = new MyWorker()
			/**
			 * 处理最后每个worker的结果
			 */
			worker.onmessage = e => {
				const { index, hash } = e.data
				//res是当前worker的resolve回调
				const res = this.callbacks.get(index)
				//返回最终结果，确保顺序
				res && res({ index, hash })
				// 删除(结果已经返回，删除数据)
				this.callbacks.delete(index)
				// worker重新变为空闲
				this.idleWorkers.push(worker)
				// 尝试执行下一个任务
				this.runNext()
			}
			this.idleWorkers.push(worker)
			this.workers.push(worker)
		}
	}

	destroy() {
		this.workers.forEach(worker => worker.terminate())
		this.workers = []
		this.idleWorkers = []
		this.queue = []
		this.callbacks.clear()
	}

	/**
	 * 提交任务
	 * taskData:{ chunk , index }
	 */
	runTask(taskData) {
		return new Promise(resolve => {
			this.callbacks.set(taskData.index, resolve)
			this.queue.push(taskData)
			this.runNext()
		})
	}

	/**
	 * 执行任务
	 */
	runNext() {
		/**
		 * 没有空闲worker 或 没任务 → 退出
		 */
		if (this.idleWorkers.length === 0 || this.queue.length === 0) return
		const worker = this.idleWorkers.shift()
		const task = this.queue.shift()
		worker.postMessage(task)
	}
}

const getChunks = file => {
	const chunks = []
	//3MB为一片
	const chunkSize = 3 * 1024 * 1024
	//切片数目
	const chunkNum = Math.ceil(file.size / chunkSize)
	for (let i = 0; i < chunkNum; i++) {
		const start = i * chunkSize
		const end = start + chunkSize
		/**
		 * 0:未上传
		 * 1:上传中
		 * 2:已上传
		 */
		chunks[i] = {
			blob: file.slice(start, end),
			status: 0,
			index: i,
		}
	}
	return chunks
}

const calculateFileHash = async file => {
	const workerPool = new WorkerPool()
	const chunks = getChunks(file)
	//任务队列
	const tasks = []
	//提交任务
	for (let i = 0; i < chunks.length; i++) {
		tasks.push(
			workerPool.runTask({
				chunk: chunks[i].blob,
				index: chunks[i].index,
			})
		)
	}
	const res = await Promise.all(tasks)
	workerPool.destroy()
	const chunkHashes = []
	//按顺序组装最后的hash数组
	res.forEach(r => {
		chunkHashes[r.index] = r.hash
	})

	const spark = new SparkMD5()
	chunkHashes.forEach(hash => spark.append(hash))
	const fileHash = spark.end()

	return fileHash
}

class RequestPool {
	//单一实例，避免并发过多
	static instance = null
	static maxConcurrency = 4
	constructor() {
		if (RequestPool.instance) return RequestPool.instance
		this.currentActive = 0
		this.queue = []
		RequestPool.instance = this
	}
	//taskId用于辨别是否为同一组任务，方便取消同一组任务，文件分片上传的id可以用hash表示
	runTask(task, taskId) {
		return new Promise((resolve, reject) => {
			this.queue.push({ task, taskId, resolve, reject })
			this.runNext()
		})
	}

	runNext() {
		if (
			this.currentActive === RequestPool.maxConcurrency ||
			this.queue.length === 0
		)
			return
		const { task, resolve, reject } = this.queue.shift()
		this.currentActive++
		task()
			.then(resolve)
			.catch(reject)
			.finally(() => {
				this.currentActive--
				this.runNext()
			})
	}

	cancelTask(taskId) {
		this.queue = this.queue.filter(item => item.taskId !== taskId)
	}
}

class FileUploader {
	/**
	 * chunks:[{ blob , status }]
	 * blob: blob
	 * status: 0(未上传),1(上传中),2(已上传)
	 *
	 * state:reactive({
	 *		progress: 0,
	 *		isPaused: false,
	 *		isUploaded: false
	 *	})
	 */

	constructor(file, state, api, retry = 3) {
		this.requestPool = new RequestPool()
		this.file = file
		//上传所需api,不同文件上传对于的接口可能不一样
		this.api = api
		// 单个分片重试次数
		this.retry = retry
		// 状态
		this.state = state
		this.chunks = []
		//正在上传的分片的controller集合（用于取消正在上传的分片）
		this.activeControllers = new Set()
		this.fileHash = ''
		this.loadedSize = 0
		// 暂停控制
		this.pausePromise = null
		this.pauseResolve = null
		this.pauseReject = null
		//上传后获得的url
		this.url = null
	}

	async init() {
		this.chunks = getChunks(this.file)
		this.fileHash = await calculateFileHash(this.file)
		const [res1, res2] = await Promise.all([
			this.api.isUploadApi(this.fileHash + getSuffix(this.file)),
			getChunksApi(this.fileHash),
		])
		this.state.isUploaded = res1.data.isUpload
		this.url = res1.data.url
		const uploadIndex = res2.data
		uploadIndex.forEach(i => {
			this.chunks[i].status = 2
			this.loadedSize += this.chunks[i].blob.size
		})
		this.state.progress = (this.loadedSize / this.file.size) * 100
		this.state.isPaused = false
		this.state.isCanceled = false
		this.pausePromise = null
		this.pauseResolve = null
		this.pauseReject = null
		removeController(this.activeControllers)
		this.activeControllers.clear()
	}

	async uploadFile() {
		await this.init()
		if (this.state.isUploaded) {
			this.state.progress = 100
			return this.url
		} else {
			const res = []
			this.chunks.forEach(chunk => {
				if (!this.state.isCanceled && chunk.status === 0) {
					res.push(
						this.requestPool.runTask(() => {
							return this.uploadChunk(chunk)
						}, this.fileHash)
					)
				}
			})
			return Promise.all(res)
				.then(async () => {
					const res = await this.mergeChunks()
					return res.data
				})
				.catch(error => {
					console.log(error)
					return false
				})
		}
	}

	async uploadChunk(chunk) {
		chunk.status = 1

		for (let attempt = 1; attempt <= this.retry; attempt++) {
			if (this.state.isCanceled) return Promise.reject()
			await this.waitIfPaused()
			const controller = new AbortController()
			this.activeControllers.add(controller)
			addController(controller)
			//本次增量,用于失败回退
			let diff = 0
			try {
				const form = new FormData()
				form.append('chunk', chunk.blob, this.getHashName(chunk.index))
				let preSize = 0
				await uploadChunkApi(
					form,
					e => {
						diff = e.loaded
						this.loadedSize += e.loaded - preSize
						preSize = e.loaded
						const percent = (this.loadedSize / this.file.size) * 100
						//合并成功前只能达到99%
						this.state.progress = Math.max(
							this.state.progress,
							Math.min(Math.floor(percent), 99)
						)
					},
					controller.signal
				).catch(error => {
					console.log(error)
				})
				chunk.status = 2
				return true
			} catch (err) {
				chunk.status = 0
				this.loadedSize -= diff
				if (this.state.isCanceled) return Promise.reject()
				if (this.state.isPaused) attempt--
				if (attempt === this.retry) {
					console.error(`chunk ${chunk.index} 上传失败`)
					throw err
				}
			} finally {
				this.activeControllers.delete(controller)
				removeController(controller)
			}
		}
	}

	async mergeChunks() {
		try {
			const res = await this.api.mergeChunksApi(
				this.fileHash + getSuffix(this.file)
			)
			this.state.progress = 100
			this.state.isUploaded = true
			return res
		} catch (error) {
			return null
		}
	}

	/**
	 * 暂停控制
	 */
	waitIfPaused() {
		if (!this.state.isPaused) return Promise.resolve()
		//只创建一次 promise
		if (!this.pausePromise) {
			this.pausePromise = new Promise((resolve, reject) => {
				this.pauseResolve = resolve
				this.pauseReject = reject
			})
		}
		return this.pausePromise
	}

	pause() {
		this.state.isPaused = true
		this.activeControllers.forEach(controller => controller.abort())
		removeController(this.activeControllers)
		this.activeControllers.clear()
	}

	resume() {
		if (this.state.isCanceled) return
		this.state.isPaused = false
		if (this.pauseResolve) {
			this.pauseResolve()
			this.pausePromise = null
			this.pauseResolve = null
			this.pauseReject = null
		}
	}

	async cancel() {
		this.state.isCanceled = true
		if (this.pauseReject) {
			this.pauseReject()
		}
		this.requestPool.cancelTask(this.fileHash)
		this.activeControllers.forEach(controller => controller.abort())
		removeController(this.activeControllers)
		this.activeControllers.clear()
		await cancelApi(this.fileHash)
		//取消后锁住所有功能，只有重新上传文件才行
	}

	getHashName(index) {
		return this.fileHash + '_' + index + getSuffix(this.file)
	}
}

export {
	getSuffix,
	getAudioDuration,
	getChunks,
	calculateFileHash,
	FileUploader,
	RequestPool,
}
