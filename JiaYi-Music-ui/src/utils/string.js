//判断字符的大小是一个空间还是两个空间（汉字占2，字母占1）
const isWideChar = char => {
	return /[\u4e00-\u9fa5\u3040-\u309f\u30a0-\u30ff]/.test(char)
}
/**
 * 返回按视觉的字符串长度和截取后的字符串
 * @param str
 * @param subLength 按索引的截取长度
 */
const handleString = (str, subLength = 0) => {
	let size = 0
	let index = 0
	for (let c of str) {
		size += isWideChar(c) ? 2 : 1
		if (size <= subLength) {
			index++
		}
	}
	return [size, str.slice(0, index)]
}
export { isWideChar, handleString }
