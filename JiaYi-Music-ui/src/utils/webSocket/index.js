class WebSocketClient {
	static instance = null

	static getInstance(url) {
		if (!this.instance) {
			this.instance = new WebSocketClient(url)
		}
		return this.instance
	}

	constructor(url) {
		this.url = url
		this.ws = null

		// 连接状态
		this.isConnected = false
		this.isManualClose = false

		// 本机网络状态字段
		this.isOnline = navigator.onLine

		// 心跳
		this.heartbeatTimer = null
		this.heartbeatInterval = 3000
		this.heartbeatTimeout = null
		this.maxWaiteTime = 60000

		// 重连配置
		this.reconnectTimer = null
		this.reconnectInterval = 3000
		this.maxReconnectAttempts = 10
		this.currentReconnectAttempts = 0

		// 监听网络变化，同步更新字段
		this.bindNetworkListener()
	}

	connect() {
		// 手动关闭 → 不连
		if (this.isManualClose) return

		// 本机无网络 → 直接不创建 WS 连接
		if (!this.isOnline) {
			console.log('当前无网络，暂不连接 WebSocket')
			return
		}

		// 已打开 → 不重复连
		if (this.ws?.readyState === WebSocket.OPEN) return

		try {
			this.ws = new WebSocket(this.url)
			this.bindEvents()
		} catch (err) {
			this.reconnect()
		}
	}

	bindEvents() {
		this.ws.onopen = () => {
			console.log('webSocket 连接成功')
			this.isConnected = true
			this.currentReconnectAttempts = 0
			this.startHeartbeat()
		}

		this.ws.onmessage = event => {
			try {
				const data = JSON.parse(event.data)
				this.onMessage(data)
			} catch (e) {}
		}

		this.ws.onclose = () => {
			console.log('WebSocket 已断开')
			this.isConnected = false
			this.clearHeartbeat()

			if (!this.isManualClose) {
				this.reconnect()
			}
		}

		this.ws.onerror = () => {
			console.error('WebSocket 异常')
			this.isConnected = false
			this.clearHeartbeat()
			this.reconnect()
		}
	}

	// 外部重写消息处理
	onMessage(data) {
		if (data.connect) {
			this.clearHeartout()
		}
	}

	send(data) {
		if (this.ws?.readyState === WebSocket.OPEN) {
			this.ws.send(JSON.stringify(data))
		}
	}

	// 手动关闭不再重连
	close() {
		this.isManualClose = true
		this.clearAll()
		this.ws?.close()
		this.isConnected = false
	}

	// 心跳
	startHeartbeat() {
		this.clearHeartbeat()
		this.heartbeatTimer = setInterval(() => {
			this.send({
				connect: 'ping',
			})
		}, this.heartbeatInterval)
		this.heartbeatTimeout = setTimeout(() => {
			this.close()
			this.isManualClose = false
			this.reconnect()
		}, this.maxWaiteTime)
	}

	reconnect() {
		// 手动关闭 → 不重连
		if (this.isManualClose) return

		if (!this.isOnline) {
			console.log('无网络，暂停重连，等待网络恢复')
			return
		}

		// 达到最大次数 → 停止
		if (this.currentReconnectAttempts >= this.maxReconnectAttempts) {
			console.error(' 已达最大重连次数，等待网络恢复或手动重连')
			return
		}

		// 防止重复定时器
		if (this.reconnectTimer) return

		this.currentReconnectAttempts++
		this.reconnectTimer = setTimeout(() => {
			this.reconnectTimer = null
			this.connect()
		}, this.reconnectInterval)
	}

	clearReconnect() {
		if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
		this.reconnectTimer = null
	}
	clearHeartout() {
		if (this.heartbeatTimeout) clearTimeout(this.heartbeatTimeout)
	}
	clearHeartbeat() {
		if (this.heartbeatTimer) clearInterval(this.heartbeatTimer)
	}

	clearAll() {
		this.clearHeartbeat()
		this.clearReconnect()
		this.clearHeartout()
	}

	//监听网络情况
	bindNetworkListener() {
		window.addEventListener('online', () => {
			console.log('网络已恢复')
			this.isOnline = true
			this.currentReconnectAttempts = 0
			this.connect()
		})

		window.addEventListener('offline', () => {
			console.log('网络已断开')
			this.isOnline = false
		})
	}
}

export default WebSocketClient
