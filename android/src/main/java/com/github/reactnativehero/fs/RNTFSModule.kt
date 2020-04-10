package com.github.reactnativehero.fs

import com.facebook.react.bridge.*
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class RNTFSModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "RNTFS"
    }

    @ReactMethod
    fun exists(path: String, promise: Promise) {

        val file = File(path)

        val list = Arguments.createArray()
        list.pushBoolean(file.exists())

        promise.resolve(list)

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

        val list = Arguments.createArray()
        list.pushBoolean(file.deleteRecursively())

        promise.resolve(list)

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
            var result = bigInt.toString(16)
            // Fill to 32 chars
            result = String.format("%32s", result).replace(' ', '0')

            val list = Arguments.createArray()
            list.pushString(result)

            promise.resolve(list)

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
            promise.reject("1", "file is not found.")
            return false
        }

        return true

    }

}