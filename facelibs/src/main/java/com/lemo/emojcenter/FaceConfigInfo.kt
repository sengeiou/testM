package com.lemo.emojcenter

import android.text.TextUtils

import com.lemo.emojcenter.constant.FaceIConstants
import com.lemo.emojcenter.utils.SDPathUtil

import java.io.File


/**
 * Created by wangru
 * Date: 2018/3/1  15:17
 * mail: 1902065822@qq.com
 * describe:
 */

object FaceConfigInfo {
    //是否是线下地址
    var isOffLineUrl: Boolean = false
    //是否是测试模式
    var  isDebug: Boolean = false

    private var rootPath: String? = null
    val APP_PATH_ROOT = "faceDemo"

    val homeUrl: String
        get() = if (isOffLineUrl) {
            FaceIConstants.HOME_URL_DEBUG
        } else FaceIConstants.HOME_URL_RELEASE

    val appKeyValue: String
        get() = if (isOffLineUrl) {
            FaceIConstants.APP_KEY_VALUE_DEBUG
        } else FaceIConstants.APP_KEY_VALUE_RELEASE

    //表情根目录
    val dirEmojRoot: String
        get() {
            val dir = getRootPath() + File.separator + "emoj"
            if (!File(dir).exists()) {
                File(dir).mkdirs()
            }
            return dir
        }

    //表情压缩包根目录
    val dirEmojZipRoot: String
        get() {
            val dir = getRootPath() + File.separator + "zip"
            if (!File(dir).exists()) {
                File(dir).mkdirs()
            }
            return dir
        }

    //收藏表情目录
    val dirCollect: String
        get() {
            val dir = getRootPath() + File.separator + "collect"
            if (!File(dir).exists()) {
                File(dir).mkdirs()
            }
            return dir
        }

    fun getDirEmoj(faceId: String): String {
        return dirEmojRoot + File.separator + faceId
    }

    fun getRootPath(): String? {
        if (TextUtils.isEmpty(rootPath)) {
            synchronized(FaceConfigInfo::class.java) {
                if (TextUtils.isEmpty(rootPath)) {
                    rootPath = SDPathUtil.getSDCardPrivateCacheDir(FaceInitData.context, "biaoqing")
                }
            }
        }
        return rootPath
    }

    //表情包icon
    fun getPathFaceIcon(faceId: String): String {
        val dir = getDirEmoj(faceId)
        if (!File(dir).exists()) {
            File(dir).mkdirs()
        }
        return dir + File.separator + "icon.png"
    }

    //表情包cover
    fun getPathFaceCover(faceId: String): String {
        val dir = getDirEmoj(faceId)
        if (!File(dir).exists()) {
            File(dir).mkdirs()
        }
        return dir + File.separator + "cover.png"
    }

    fun getCollectPathByUrl(url: String): String? {
        if (!TextUtils.isEmpty(url)) {
            val filename = url.substring(url.lastIndexOf("/") + 1, url.length)
            return dirCollect + File.separator + filename
        }
        return null
    }

}
