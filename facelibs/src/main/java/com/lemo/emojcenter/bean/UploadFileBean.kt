package com.lemo.emojcenter.bean

import android.text.TextUtils

import com.lemo.emojcenter.utils.PathUtils

import java.io.File

/**
 * Description : 上传文件信息
 * Author :fengjing
 * Email :164303256@qq.com
 * Date :2016/10/13
 */
class UploadFileBean(
        /**
         * 本地文件地址
         */
        var localPath: String?,
        /**
         * 上传的文件夹
         */
        var folder: String?) {
    /**
     * 上传到服务器端使用的文件名 随机生成
     */
    var uploadFileName: String? = null

    /**
     * 判断文件信息是否合法
     */
    val isIllegal: Boolean
        get() {
            if (TextUtils.isEmpty(localPath) || TextUtils.isEmpty(folder) || TextUtils.isEmpty(uploadFileName)) {
                return true
            }
            val file = File(localPath!!)
            return !file.exists()
        }

    init {
        val file = File(localPath)
        if (file.exists()) {
            this.uploadFileName = PathUtils.getUploadFilePath(file.name)
        }
    }
}
