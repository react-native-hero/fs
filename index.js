
import { NativeModules } from 'react-native'

const { RNTFS } = NativeModules

export default {

  /**
   * 判断文件是否存在
   */
  exists(path) {
    return RNTFS.exists(path)
  },

  /**
   * 获取文件信息
   */
  stat(path) {
    return RNTFS.stat(path)
  },

  /**
   * 删除文件
   */
  unlink(path) {
    return RNTFS.unlink(path)
  },

  /**
   * 获取文件 md5
   */
  md5(path) {
    return RNTFS.md5(path)
  },

}
