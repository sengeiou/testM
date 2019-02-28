package com.qingmeng.mengmeng.utils

import android.annotation.SuppressLint
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object PathUtils {
    val DIR_NAME = "mengmeng/temp"
    private var tempDir: File? = null

    val apkPath: String
        get() {
            if (tempDir == null) {
                tempDir = File(Environment.getExternalStorageDirectory(), DIR_NAME)
                if (!tempDir!!.exists()) {
                    tempDir!!.mkdirs()
                }
            }
            val path = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val dir = File(tempDir, "mengmeng_$path.apk")
            return dir.path
        }

    init {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            tempDir = File(Environment.getExternalStorageDirectory(), DIR_NAME)
            if (!tempDir!!.exists()) {
                tempDir!!.mkdirs()
            }
        }
    }
}
