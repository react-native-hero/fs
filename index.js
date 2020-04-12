
import { NativeModules, Platform } from 'react-native'

const { RNTFS } = NativeModules

export const CODE = Platform.select({
  ios: {
    FILE_NOT_FOUND: RNTFS.ERROR_CODE_FILE_NOT_FOUND,
  },
  android: {
    FILE_NOT_FOUND: RNTFS.ERROR_CODE_FILE_NOT_FOUND,
    MD5_ALGORITHM_NOT_FOUND: RNTFS.ERROR_CODE_MD5_ALGORITHM_NOT_FOUND,
    MD5_CALCULATE_FAILURE: RNTFS.ERROR_CODE_MD5_CALCULATE_FAILURE,
    SCANNER_NOT_CONNECTED: RNTFS.ERROR_CODE_SCANNER_NOT_CONNECTED,
  }
})

export const DIRECTORY = {
  CACHE: RNTFS.DIRECTORY_CACHE,
  DOCUMENT: RNTFS.DIRECTORY_DOCUMENT,
  DOWNLOAD: RNTFS.DIRECTORY_DOWNLOAD,
  PICTURE: RNTFS.DIRECTORY_PICTURE,
  MUSIC: RNTFS.DIRECTORY_MUSIC,
  MOVIE: RNTFS.DIRECTORY_MOVIE,
}

/**
 * 判断文件是否存在
 */
export function exists(path) {
  return RNTFS.exists(path)
}

/**
 * 获取文件信息
 */
export function stat(path) {
  return RNTFS.stat(path)
  .then(data => {
    // 底层为了避免损失精度，返回了时间戳字符串
    // 这里转换为 number，方便外部直接 new Date(data.mtime)
    data.mtime = +data.mtime
    return data
  })
}

/**
 * 删除文件
 */
export function unlink(path) {
  return RNTFS.unlink(path)
}

/**
 * 获取文件 md5
 */
export function md5(path) {
  return RNTFS.md5(path)
}

/**
 * 扫描文件，以便能被资源管理器识别
 */
export const scan = Platform.select({
  ios() {
    throw new Error('scan can be called on android only.')
  },
  android(options) {
    return RNTFS.scan(options)
  }
})