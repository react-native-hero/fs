package com.github.reactnativehero.fs

import com.facebook.react.bridge.*
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.HashMap

class RNTFSModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val ERROR_CODE_FILE_NOT_FOUND = "1"
    }

    override fun getName(): String {
        return "RNTFS"
    }

    override fun getConstants(): Map<String, Any>? {

        val constants: MutableMap<String, Any> = HashMap()

        constants["ERROR_CODE_FILE_NOT_FOUND"] = ERROR_CODE_FILE_NOT_FOUND

        return constants

    }

    @ReactMethod
    fun exists(path: String, promise: Promise) {

        val file = File(path)

        val map = Arguments.createMap()
        map.putBoolean("existed", file.exists())

        promise.resolve(map)

    }

    @ReactMethod
    fun stat(path: String, promise: Promise) {

        val file = File(path)

        if (!checkFileExisted(file, promise)) {
            return
        }

        val map = Arguments.createMap()
        map.putInt("size", file.length().toInt())
        map.putInt("mtime", file.lastModified().toInt())

        promise.resolve(map)

    }

    @ReactMethod
    fun unlink(path: String, promise: Promise) {

        val file = File(path)

        if (!checkFileExisted(file, promise)) {
            return
        }

        val map = Arguments.createMap()
        map.putBoolean("success", file.deleteRecursively())

        promise.resolve(map)

    }

    @ReactMethod
    fun md5(path: String, promise: Promise) {

        val file = File(path)

        if (!checkFileExisted(file, promise)) {
            return
        }

        val digest: MessageDigest = try {
            MessageDigest.getInstance("MD5")
        }
        catch (e: NoSuchAlgorithmException) {
            return
        }

        val inputStream: InputStream = try {
            FileInputStream(file)
        }
        catch (e: FileNotFoundException) {
            return
        }

        val buffer = ByteArray(8192)
        var read: Int
        try {

            while (inputStream.read(buffer).also { read = it } > 0) {
                digest.update(buffer, 0, read)
            }
            val md5sum = digest.digest()
            val bigInt = BigInteger(1, md5sum)
            val result = bigInt.toString(16)

            val map = Arguments.createMap()
            map.putString(
                "md5",
                String.format("%32s", result).replace(' ', '0')
            )

            promise.resolve(map)

        }
        catch (e: IOException) {
            throw RuntimeException("Unable to process file for MD5", e)
        }
        finally {
            try {
                inputStream.close()
            } catch (e: IOException) {

            }
        }

    }

    private fun checkFileExisted(file: File, promise: Promise): Boolean {

        if (!file.exists()) {
            promise.reject(ERROR_CODE_FILE_NOT_FOUND, "file is not found.")
            return false
        }

        return true

    }

}