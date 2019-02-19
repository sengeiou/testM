package com.lemo.emojcenter.utils

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.lemo.emojcenter.FaceConfigInfo
import com.lemo.emojcenter.FaceInitData
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 打印日志到手机文件
 * Created by wangru
 * Date: 2017/9/12  19:47
 * mail: 1902065822@qq.com
 * describe:
 */
object ULogToDevice {
    private val LOG_MAX_LENGTH = (200 * 1024).toLong()//200k 每个日志文件最大大小(B)
    private val VERBOSE = 'v'
    private val DEBUG = 'd'
    private val INFO = 'i'
    private val WARN = 'w'
    private val ERROR = 'e'
    private val TAG = ULogToDevice::class.java.simpleName
    private val logDir = getSDCardPublicDir(FaceInitData.context, FaceConfigInfo.APP_PATH_ROOT + "/log")//log日志存放路径
    private val IS_DEBUG = FaceConfigInfo.isDebug
    private var filePath: String? = null
    private var fileName: String? = null
    private var adminName: String? = null


    fun v(tag: String, msg: String) {
        v(null, tag, msg)
    }

    fun d(tag: String, msg: String) {
        d(null, tag, msg)
    }

    fun i(tag: String, msg: String) {
        i(null, tag, msg)
    }

    fun w(tag: String, msg: String) {
        w(null, tag, msg)
    }

    fun e(tag: String, msg: String) {
        e(null, tag, msg)
    }

    fun v(filename: String?, tag: String, msg: String) {
        write(VERBOSE, filename, tag, msg)
    }

    fun d(filename: String?, tag: String, msg: String) {
        write(DEBUG, filename, tag, msg)
    }

    fun i(filename: String?, tag: String, msg: String) {
        write(INFO, filename, tag, msg)
    }

    fun w(filename: String?, tag: String, msg: String) {
        write(WARN, filename, tag, msg)
    }

    fun e(filename: String?, tag: String, msg: String) {
        write(ERROR, filename, tag, msg)
    }

    fun write(type: Char, filename: String?, tag: String, msg: String) {
        if (IS_DEBUG) {
            fileName = filename
            writeToFile(type, tag, msg)
        }
    }

    /**
     * 将log信息写入文件中
     *
     * @param type
     * @param tag
     * @param msg
     */
    private fun writeToFile(type: Char, tag: String, msg: String) {
        if (null == logDir) {
            return
        }
        var fos: FileOutputStream? = null//FileOutputStream会自动调用底层的close()方法，不用关闭
        var bw: BufferedWriter? = null
        try {
            val log = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US).format(Date()) + " " + type + " " + tag + "\n" + msg + "\n\n\n"//log日志内容，可以自行定制
            filePath = getLogPath(null)
            fos = FileOutputStream(filePath!!, true)//这里的第二个参数代表追加还是覆盖，true为追加，flase为覆盖
            bw = BufferedWriter(OutputStreamWriter(fos))
            bw.write(log)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (bw != null) {
                    bw.close()//关闭缓冲流
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    //获取文件大小
    private fun getFileSize(filePath: String): Long {
        if (TextUtils.isEmpty(filePath)) {
            return 0
        }
        val file = File(filePath) ?: return 0
        var size: Long = 0
        if (file.exists()) {
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
                size = fis.available().toLong()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (fis != null) {
                    try {
                        fis.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

        }
        return size
    }

    //得到文件名
    private fun getLogPath(path: String?): String {
        if (TextUtils.isEmpty(path)) {
            filePath = appPath() + ".log"
        }
        val logFile = File(filePath!!)
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        val logFileSize = getFileSize(filePath!!)
        if (logFileSize >= LOG_MAX_LENGTH) {
            var temp = 0
            if (filePath!!.contains("(")) {
                temp = Integer.parseInt(filePath!!.substring(filePath!!.indexOf("(") + 1, filePath!!.lastIndexOf(")")))
            }
            filePath = appPath() + "(" + ++temp + ")" + ".log"
            return getLogPath(filePath)
        } else {
            return filePath as String
        }
    }

    private fun appPath(): String {
        val time = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        var dir = logDir + File.separator + time
        if (!TextUtils.isEmpty(getAdminName())) {
            dir = dir + File.separator + getAdminName()
        }
        if (!File(dir).exists()) {
            File(dir).mkdirs()
        }
        return dir + "/log" + if (TextUtils.isEmpty(fileName)) "" else "_" + fileName!!
    }

    fun getSDCardPublicDir(context: Context, filedir: String): String {
        var path: String
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
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

    private fun getAdminName(): String? {
        if (TextUtils.isEmpty(adminName)) {
            synchronized(ULogToDevice::class.java) {
                if (TextUtils.isEmpty(adminName)) {
                    val fileinfo = loadFileFromSDCard(logDir + "/info.txt")
                    val content = String(fileinfo!!)
                    val pathInfo = Gson().fromJson(content, PathInfo::class.java)
                    adminName = pathInfo.dirName
                    Log.d(TAG, "getAdminName: " + adminName!!)
                }
            }
        }
        return adminName
    }

    fun setAdminName(adminName: String) {
        ULogToDevice.adminName = adminName
        val pathInfo = PathInfo()
        pathInfo.dirName = adminName
        val path = logDir + "/info.txt"
        if (File(path).exists()) {
            File(path).delete()
        }
        saveFileToSD(path, Gson().toJson(pathInfo).toString().toByteArray())
    }

    fun saveFileToSD(filepath: String, bytes: ByteArray) {
        try {
            val output = FileOutputStream(filepath, false)
            output.write(bytes)//将bytes写入到输出流中
            output.close() //关闭输出流
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    // 从SD卡获取文件
    fun loadFileFromSDCard(fileDir: String): ByteArray? {
        var bis: BufferedInputStream? = null
        val baos = ByteArrayOutputStream()
        try {
            bis = BufferedInputStream(FileInputStream(File(fileDir)))
            val buffer = ByteArray(8 * 1024)
            var c: Int?=null
            while ({c = bis!!.read(buffer); c}() != -1) {
                c?.let {
                    baos.write(buffer, 0, it)
                    baos.flush()
                }

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


    internal class PathInfo {
        var dirName: String?=null
        var fileName: String?=null
    }
}
