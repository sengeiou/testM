package com.lemo.emojcenter.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author fengjing:
 * @function
 * @date ：2016年6月28日 下午1:46:33
 * @mail 164303256@qq.com
 */
object PathUtils {
    /**
     * 存放临时文件
     */
    private val DIR_TEMP = "xiaozhu/temp"
    /**
     * 存放日志文件
     */
    private val DIR_LOG = "xiaozhu/log"
    var logDir: File? = null
    private var tempDir: File? = null

    val localImgPath: String?
        get() {
            if (tempDir == null) {
                return null
            }
            val path = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val dir = File(tempDir, "temp_$path.jpg")
            return dir.path
        }

    //System.currentTimeMillis()+".amr"
    val apkPath: String?
        get() {
            if (tempDir == null) {
                return null
            }
            val path = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val dir = File(tempDir, "xiaozhu_$path.apk")
            return dir.path
        }

    /**
     * 获取裁剪图片保存位置
     *
     * @return
     */
    val tailorImgPath: String?
        get() {
            if (tempDir == null) {
                return null
            }
            val path = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val dir = File(tempDir, "temp_$path.jpg")
            return dir.path
        }

    val logPath: String?
        get() {
            if (logDir == null) {
                return null
            }
            val path = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val dir = File(logDir, "xiaozhu_$path.log")
            return dir.path
        }

    //获取系统分配给应用的总内存大小
    val memorySize: Int
        get() {
            val maxMemory = Runtime.getRuntime().maxMemory().toInt()
            return maxMemory / 10
        }

    /**
     * 获取缓存长度
     *
     * @return
     */
    val cacheSize: Long
        get() {
            var length: Long = 0
            length += getFolderLength(tempDir)
            length += getFolderLength(logDir)
            return length
        }

    init {
        if (android.os.Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED) {
            tempDir = File(android.os.Environment.getExternalStorageDirectory(), DIR_TEMP)
            if (!tempDir!!.exists()) {
                tempDir!!.mkdirs()
            }
            logDir = File(android.os.Environment.getExternalStorageDirectory(), DIR_LOG)

            if (!logDir!!.exists()) {
                logDir!!.mkdirs()
            }
        }
    }

    fun getBabyImgPath(position: Int): String? {
        if (tempDir == null) {
            return null
        }
        val path = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val dir = File(tempDir, "babyimg_$position.jpg")
        return dir.path
    }

    /**
     * 上传到阿里云前本地生成随机地址
     *
     * @param fileName 本地文件名
     * @return
     */
    fun getUploadFilePath(fileName: String): String {
        //扩展名
        var extensionName: String? = null
        if (!TextUtils.isEmpty(fileName)) {
            val lastIndex = fileName.lastIndexOf(".")
            if (lastIndex != -1) {
                extensionName = fileName.substring(lastIndex, fileName.length)
            }
        }
        val result = UUID.randomUUID().toString()
        return result + extensionName!!
    }

    /**
     * 获取文件夹下文件总的大小<只扫描一级目录></只扫描一级目录>>
     *
     * @param file
     * @return
     */
    fun getFolderLength(file: File?): Long {
        if (file == null || !file.exists()) {
            return 0
        }
        var length = 0
        val files = file.listFiles()
        if (files != null) {
            for (i in files.indices) {
                length += files[i].length().toInt()
            }
        }
        return length.toLong()
    }

    //往SD卡写入文件的方法
    fun saveBitmapToSDCard(bitmap: Bitmap, path: String): String? {
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(path)
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.close()
            }
            return path
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 复制文件
     *
     * @param source 输入文件
     * @param target 输出文件
     */
    fun copy(source: File, target: File) {
        var fileInputStream: FileInputStream? = null
        var fileOutputStream: FileOutputStream? = null
        try {
            fileInputStream = FileInputStream(source)
            fileOutputStream = FileOutputStream(target)
            val buffer = ByteArray(1024)
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close()
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 图片是否完整
     */
    fun isImageComplete(filePath: String): Boolean {
        if (TextUtils.isEmpty(filePath) || !File(filePath).exists()) {
            return false
        }
        var options: BitmapFactory.Options? = null
        if (options == null) {
            options = BitmapFactory.Options()
        }
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        return !(options.mCancel || options.outWidth == -1 || options.outHeight == -1)
    }
}
