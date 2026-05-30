import SparkMD5 from 'spark-md5'

self.onmessage = e => {
	/**
	 * chunk：文件分片（Blob对象）
	 * index：分片索引（用于排序）,必要！不同顺序构建的hash不一样
	 */
	const { chunk, index } = e.data
	const reader = new FileReader()

	/**
	 * SparkMD5：用于计算 hash
	 * ArrayBuffer 版本用于处理二进制
	 */
	const spark = new SparkMD5.ArrayBuffer()

	reader.onload = () => {
		spark.append(reader.result)
		const hash = spark.end()
		self.postMessage({
			index,
			hash,
		})
	}
	reader.readAsArrayBuffer(chunk)
}
