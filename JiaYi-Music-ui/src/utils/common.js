//防抖
function debounce(fn, delay = 300) {
	let timer = null
	return function (...args) {
		if (timer) {
			clearTimeout(timer)
			setTimeout(() => {
				fn.apply(this, args)
			}, delay)
		}
	}
}

//节流
function throttle(fn, interval = 300) {
	let preTime = 0
	return function (...args) {
		const now = Date.now()
		if (now - preTime >= interval) {
			preTime = now
			fn.apply(this, args)
		}
	}
}

function formatTime(seconds) {
	const min = Math.floor(seconds / 60)
	const sec = seconds % 60
	// 补零：1 → 01
	return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
}

class PausableTimer {
	constructor(delay) {
		this.delay = delay
		this.timer = null
		this.remainTime = delay
		this.startStamp = 0
		this.cb = null
	}

	// 单独开始：传入回调
	start(callback) {
		if (this.timer) return
		this.cb = callback
		this.startStamp = Date.now()
		this.timer = setTimeout(() => {
			this.cb?.()
			this.reset()
		}, this.remainTime)
	}

	// 暂停
	pause() {
		if (!this.timer) return
		clearTimeout(this.timer)
		this.timer = null
		this.remainTime -= Date.now() - this.startStamp
	}

	// 继续（不用再传回调）
	resume() {
		if (this.timer || this.remainTime <= 0) return
		this.startStamp = Date.now()
		this.timer = setTimeout(() => {
			this.cb?.()
			this.reset()
		}, this.remainTime)
	}

	// 重置
	reset() {
		clearTimeout(this.timer)
		this.timer = null
		this.remainTime = this.delay
		this.cb = null
	}
}

export { debounce, throttle, formatTime, PausableTimer }
