package com.github.reactnativehero.fs

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.facebook.react.bridge.*
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class RNTFSModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val ERROR_CODE_FILE_NOT_FOUND = "1"
        private const val ERROR_CODE_MD5_ALGORITHM_NOT_FOUND = "2"
        private const val ERROR_CODE_MD5_CALCULATE_FAILURE = "3"
        private const val ERROR_CODE_PREVIEW_APP_NOT_FOUND = "4"
        private const val ERROR_CODE_SCANNER_NOT_CONNECTED = "5"
    }

    private var scanner: MediaScannerConnection
    private var scanTasks = hashMapOf<String, Promise>()

    init {
        scanner = MediaScannerConnection(reactContext, object : MediaScannerConnection.MediaScannerConnectionClient {
            override fun onMediaScannerConnected() {

            }

            override fun onScanCompleted(path: String, uri: Uri?) {
                if (scanTasks.contains(path)) {
                    val promise = scanTasks[path]
                    val map = Arguments.createMap()
                    map.putString("path", path)
                    promise?.resolve(map)
                    scanTasks.remove(path)
                }
            }
        })
        scanner.connect()
    }

    override fun invalidate() {
        super.invalidate()
        scanner.disconnect()
    }

    override fun getName(): String {
        return "RNTFS"
    }

    override fun getConstants(): Map<String, Any> {

        val constants: MutableMap<String, Any> = HashMap()

        constants["DIRECTORY_CACHE"] = reactContext.cacheDir.absolutePath
        constants["DIRECTORY_DOCUMENT"] = reactContext.filesDir.absolutePath

        constants["DIRECTORY_DOWNLOAD"] = reactContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath ?: ""
        constants["DIRECTORY_PICTURE"] = reactContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath ?: ""
        constants["DIRECTORY_MUSIC"] = reactContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: ""
        constants["DIRECTORY_MOVIE"] = reactContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath ?: ""

        constants["ERROR_CODE_FILE_NOT_FOUND"] = ERROR_CODE_FILE_NOT_FOUND
        constants["ERROR_CODE_MD5_ALGORITHM_NOT_FOUND"] = ERROR_CODE_MD5_ALGORITHM_NOT_FOUND
        constants["ERROR_CODE_MD5_CALCULATE_FAILURE"] = ERROR_CODE_MD5_CALCULATE_FAILURE
        constants["ERROR_CODE_PREVIEW_APP_NOT_FOUND"] = ERROR_CODE_PREVIEW_APP_NOT_FOUND
        constants["ERROR_CODE_SCANNER_NOT_CONNECTED"] = ERROR_CODE_SCANNER_NOT_CONNECTED

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
        map.putString("name", file.name)
        map.putInt("size", file.length().toInt())
        // 如果用 toInt，貌似结果是错的
        map.putString("mtime", file.lastModified().toString())
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
            promise.reject(ERROR_CODE_MD5_ALGORITHM_NOT_FOUND, e.localizedMessage)
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
            promise.reject(ERROR_CODE_MD5_CALCULATE_FAILURE, e.localizedMessage)
        }
        finally {
            try {
                inputStream.close()
            }
            catch (e: IOException) {
                promise.reject(ERROR_CODE_MD5_CALCULATE_FAILURE, e.localizedMessage)
            }
        }

    }

    @ReactMethod
    fun preview(options: ReadableMap, promise: Promise) {

        val activity = reactContext.currentActivity ?: return

        val path = options.getString("path")
        val mimeType = options.getString("mimeType")

        val file = File(path!!)
        if (!checkFileExisted(file, promise)) {
            return
        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_DEFAULT)

        val uri = FileProvider.getUriForFile(activity, reactApplicationContext.packageName + ".provider", file)
        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val list = reactContext.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (list.size > 0) {
            activity.startActivity(intent)
            val map = Arguments.createMap()
            promise.resolve(map)
        }
        else {
            promise.reject(ERROR_CODE_PREVIEW_APP_NOT_FOUND, "preview app is not found.")
        }

    }

    @ReactMethod
    fun scan(options: ReadableMap, promise: Promise) {

        val path = options.getString("path") as String
        val mimeType = options.getString("mimeType") as String

        if (!checkFileExisted(File(path), promise)) {
            return
        }

        if (scanner.isConnected) {
            scanTasks[path] = promise
            scanner.scanFile(path, mimeType)
        }
        else {
            promise.reject(ERROR_CODE_SCANNER_NOT_CONNECTED, "scanner is not connected.")
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
