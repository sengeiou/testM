package com.lemo.emojcenter.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Log

import java.io.*

/**
 * http://blog.csdn.net/jason_996/article/details/51605614
 * The type of files directory to return. May be null for the root of the files directory or one of the
 * following Environment constants for a subdirectory: [Environment.DIRECTORY_MUSIC],
 * [Environment.DIRECTORY_PODCASTS], [Environment.DIRECTORY_RINGTONES],
 * [Environment.DIRECTORY_ALARMS], [Environment.DIRECTORY_NOTIFICATIONS],
 * [Environment.DIRECTORY_PICTURES], or [Environment.DIRECTORY_MOVIES].
 * The path of the directory holding application files on external storage. Returns null if external storage is
 * not currently mounted so it could not ensure the path exists; you will need to call this method again when it
 * is available.
 * Environment#getExternalStoragePublicDirectory
 */
object SDPathUtil {
    private val TAG = SDPathUtil::class.java.simpleName

    // 判断SD卡是否被挂载
    val isSDCardMounted: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    // 获取SD卡的根目录
    val sdCardBaseDir: String?
        get() = if (isSDCardMounted) {
            Environment.getExternalStorageDirectory().absolutePath
        } else null

    // 获取SD卡的完整空间大小，返回MB
    val sdCardSize: Long
        @SuppressLint("NewApi")
        get() {
            if (isSDCardMounted) {
                val fs = StatFs(sdCardBaseDir)
                val count = fs.blockCountLong
                val size = fs.blockSizeLong
                return count * size / 1024 / 1024
            }
            return 0
        }

    // 获取SD卡的剩余空间大小
    val sdCardFreeSize: Long
        @SuppressLint("NewApi")
        get() {
            if (isSDCardMounted) {
                val fs = StatFs(sdCardBaseDir)
                val count = fs.freeBlocksLong
                val size = fs.blockSizeLong
                return count * size / 1024 / 1024
            }
            return 0
        }

    // 获取SD卡的可用空间大小
    val sdCardAvailableSize: Long
        @SuppressLint("NewApi")
        get() {
            if (isSDCardMounted) {
                val fs = StatFs(sdCardBaseDir)
                val count = fs.availableBlocksLong
                val size = fs.blockSizeLong
                return count * size / 1024 / 1024
            }
            return 0
        }

    /**
     * 获取SD卡公有目录的路径,SD卡卸载就取系统目录
     *
     * @param type Environment
     */
    fun getSDCardPublicDirByType(type: String): String {
        return if (isSDCardMounted) {
            Environment.getExternalStoragePublicDirectory(type).toString()
        } else {
            ///data
            Environment.getDataDirectory().absolutePath
        }
    }

    fun getSDCardPublicDir(context: Context, filedir: String): String {
        var path: String
        if (isSDCardMounted) {
            path = Environment.getExternalStorageDirectory().path
        } else {
            path = Environment.getDataDirectory().absolutePath
        }
        if (!TextUtils.isEmpty(filedir)) {
            path += "/" + filedir
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
        }
        return path
    }

    /**
     * 获取私有缓存目录
     *
     * @param context Context
     * @param filedir 文件夹名字 eg : zuzu/img
     * @return
     */
    @JvmOverloads
    fun getSDCardPrivateCacheDir(context: Context?, filedir: String? = null): String {
        var path = ""
        if (isSDCardMounted) {
            if (context != null && context.externalCacheDir != null) {
                path = context.externalCacheDir!!.absolutePath//  /mnt/sdcard/Android/data/com.my.app/cache
            } else if (Environment.getExternalStorageDirectory() != null) {
                path = Environment.getExternalStorageDirectory().path//  /mnt/sdcard
            }
        } else {
            if (context!!.cacheDir != null) {
                path = context.cacheDir.absolutePath//  /data/data/com.my.app/cache
            } else {
                path = Environment.getDataDirectory().absolutePath//  /data
            }

        }
        path = pathAddFile(filedir, path)
        Log.d(TAG, "目录：" + path)
        return path
    }


    // 获取SD卡私有Files目录的路径,SD卡卸载就取系统目录
    fun getSDCardPrivateCacheDirByType(context: Context, type: String): String {
        if (isSDCardMounted) {
            // SDCard/Android/data/你的应用的包名/files/typename
            return context.getExternalFilesDir(type)!!.absolutePath
        } else {
            // /data/data/com.my.app/files/typename
            var path = ""
            if (!TextUtils.isEmpty(type)) {
                path = "/" + type
            }
            return context.filesDir.absolutePath + path
        }
    }

    /**
     * 在目录添加文件
     *
     * @param filedir 某种根目录 eg:Environment.getDataDirectory()
     * @param path    文件夹 eg: zuzu/img
     * @return
     */
    private fun pathAddFile(filedir: String?, path: String): String {
        var path = path
        if (!TextUtils.isEmpty(filedir)) {
            path = path + "/" + filedir
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
        }
        return path
    }

    /**
     * 从文件路径中获取文件名带后缀 photo.png
     *
     * @param path
     * @return
     */
    fun getFileNameWithTypeForPath(path: String): String? {
        var path = path
        if (!TextUtils.isEmpty(path) && path.contains("/")) {
            val start = path.lastIndexOf("/")
            path = path.substring(start + 1, path.length)
        }
        return path
    }

    /**
     * 从文件路径中获取文件名不带后缀 photo
     *
     * @param path
     * @return
     */
    fun getFileNameForPath(path: String): String {
        var path = path
        val start = path.lastIndexOf("/")
        val end = path.lastIndexOf(".")
        if (start != -1 && end != -1) {
            path = path.substring(start + 1, end)
        }
        return path
    }

    fun isFileExist(filePath: String): Boolean {
        val file = File(filePath)
        return file.isFile
    }

    // 往SD卡的公有目录下保存文件
    fun saveFileToSDCardPublicDir(data: ByteArray, type: String, fileName: String): Boolean {
        var bos: BufferedOutputStream? = null
        if (isSDCardMounted) {
            val file = Environment.getExternalStoragePublicDirectory(type)
            try {
                bos = BufferedOutputStream(FileOutputStream(File(file, fileName)))
                bos.write(data)
                bos.flush()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    bos!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return false
    }

    // 往SD卡的自定义目录下保存文件
    fun saveFileToSDCardCustomDir(data: ByteArray, path: String): Boolean {
        var fos: FileOutputStream? = null
        var bos: BufferedOutputStream? = null
        if (isSDCardMounted) {
            try {
                fos = FileOutputStream(File(path))
                bos = BufferedOutputStream(fos)
                bos.write(data)
                bos.flush()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    bos!!.close()
                    fos!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return false
    }

    // 往SD卡的私有Files目录下保存文件
    fun saveFileToSDCardPrivateFilesDir(data: ByteArray, type: String, fileName: String, context: Context): Boolean {
        var fos: FileOutputStream? = null
        var bos: BufferedOutputStream? = null
        if (isSDCardMounted) {
            try {
                val file = context.getExternalFilesDir(type)
                fos = FileOutputStream(File(file, fileName))
                bos = BufferedOutputStream(fos)
                bos.write(data)
                bos.flush()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    bos!!.close()
                    fos!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return false
    }

    // 往SD卡的私有Cache目录下保存文件
    fun saveFileToSDCardPrivateCacheDir(data: ByteArray, fileName: String, context: Context): Boolean {
        var bos: BufferedOutputStream? = null
        if (isSDCardMounted) {
            val file = context.externalCacheDir
            try {
                bos = BufferedOutputStream(FileOutputStream(File(file, fileName)))
                bos.write(data)
                bos.flush()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (bos != null) {
                    try {
                        bos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
        return false
    }

    // 保存bitmap图片到SDCard的私有Cache目录
    fun saveBitmapToSDCardPrivateCacheDir(bitmap: Bitmap, fileName: String, context: Context): Boolean {
        if (isSDCardMounted) {
            var bos: BufferedOutputStream? = null
            // 获取私有的Cache缓存目录
            val file = context.externalCacheDir

            try {
                bos = BufferedOutputStream(FileOutputStream(File(file, fileName)))
                if (fileName.contains(".png") || fileName.contains(".PNG")) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                }
                bos.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (bos != null) {
                    try {
                        bos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
            return true
        } else {
            return false
        }
    }

    //往SD卡写入文件的方法
    @Throws(Exception::class)
    fun saveFileToSD(filepath: String, bytes: ByteArray) {
        val output = FileOutputStream(filepath, false)
        output.write(bytes)//将bytes写入到输出流中
        output.close() //关闭输出流
        //        Toast.makeText(MainApplication.getInstance(), "图片已成功保存到" + filepath, Toast.LENGTH_SHORT).show();
    }

    // 从SD卡获取文件
    fun loadFileFromSDCard(fileDir: String): ByteArray? {
        var bis: BufferedInputStream? = null
        val baos = ByteArrayOutputStream()
        try {
            bis = BufferedInputStream(FileInputStream(File(fileDir)))
            val buffer = ByteArray(8 * 1024)
            var c: Int=0
            while ({ c = bis!!.read(buffer);c }() != null) {
                baos.write(buffer, 0, c)
                baos.flush()
            }

            return baos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                baos.close()
                if (bis != null) {
                    bis.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return null
    }

    // 从SDCard中寻找指定目录下的文件，返回Bitmap
    fun loadBitmapFromSDCard(filePath: String): Bitmap? {
        val data = loadFileFromSDCard(filePath)
        if (data != null) {
            val bm = BitmapFactory.decodeByteArray(data, 0, data.size)
            if (bm != null) {
                return bm
            }
        }
        return null
    }

    // 从sdcard中删除文件
    fun removeFileFromSDCard(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            try {
                file.delete()
                true
            } catch (e: Exception) {
                false
            }

        } else {
            false
        }
    }

    /**
     * 清空文件夹
     */
    fun clearFolder(path: String) {
        clearFolder(File(path))
    }

    fun clearFolder(file: File) {
        if (file.isFile) {
            file.delete()
            return
        }
        if (file.isDirectory) {
            val childFile = file.listFiles()
            if (childFile == null || childFile.size == 0) {
                file.delete()
                return
            }
            for (f in childFile) {
                clearFolder(f)
            }
            file.delete()
        }
    }

    fun getFolderLength(path: String): Long {
        return getFolderLength(File(path))
    }

    fun getFolderLength(file: File?): Long {
        var size: Long = 0
        try {
            if (file != null) {
                val fileList = file.listFiles()
                if (fileList != null && fileList.size > 0) {
                    for (i in fileList.indices) {
                        if (fileList[i].isDirectory) {
                            size = size + getFolderLength(fileList[i])
                        } else {
                            size = size + fileList[i].length()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return size
    }


    //保存文件通知系统更新，在图库显示图片
    fun updateImageSysStatu(context: Context?, path: String) {
        if (!TextUtils.isEmpty(path)) {
            val file = File(path)
            if (context != null && file != null && file.exists()) {
                // 把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage(context.contentResolver, file.absolutePath, "img", "图片")
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                updateFileStatu(context, file)
            }

        } else {
            Log.d(TAG, "updateFileStatu: path is null")
        }
    }

    //保存文件通知系统更新，在图库显示图片
    fun updateFileStatu(context: Context?, file: File?) {
        if (context != null && file != null && file.exists()) {
            //通知图库更新
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val uri: Uri
                if (Build.VERSION.SDK_INT >= 24) {
                    uri = FileProvider.getUriForFile(context, "com.zsdudu.dudu.fileprovider", file)
                } else {
                    uri = Uri.fromFile(file)
                }
                intent.data = uri
                context.sendBroadcast(intent)
            } else {
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())))
            }
        } else {
            Log.d(TAG, "updateFileStatu: file is not exist")
        }
    }
}// 获取SD卡私有Cache目录的路径,SD卡卸载就取系统目录
