
import { NativeModules } from 'react-native'

const { RNTFS } = NativeModules

export const CODE = {
  FILE_NOT_FOUND: RNTFS.ERROR_CODE_FILE_NOT_FOUND,
}

export const DIRECTORY = {
  CACHE: RNTFS.DIRECTORY_CACHE,
  DOCUMENT: RNTFS.DIRECTORY_DOCUMENT,
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
