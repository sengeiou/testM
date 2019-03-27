package com.qingmeng.mengmeng.utils

import android.net.Uri
import com.danikula.videocache.HttpProxyCacheServer
import com.danikula.videocache.file.FileNameGenerator
import com.qingmeng.mengmeng.MainApplication
import com.qingmeng.mengmeng.constant.IConstants
import java.io.File

/**
 * 缓存网络文件
 */
class HttpProxyCacheUtil {
    private var audioProxy: HttpProxyCacheServer? = null

    fun getAudioProxy(): HttpProxyCacheServer {
        if (audioProxy == null) {
            audioProxy = HttpProxyCacheServer.Builder(MainApplication.instance)
                    .cacheDirectory(File(IConstants.DIR_AUDIO_STR))
                    //                    .maxCacheSize(1024 * 1024 * 1024) // 缓存大小
                    .fileNameGenerator(CacheFileNameGenerator())
                    .build()
        }
        return audioProxy!!
    }

    //缓存文件命名规则
    class CacheFileNameGenerator : FileNameGenerator {
        override fun generate(url: String): String {
            val uri = Uri.parse(url)
            val pathSegList = uri.pathSegments
            val path: String
            if (pathSegList != null && pathSegList.size > 0) {
                path = pathSegList[pathSegList.size - 1]
            } else {
                path = url
            }
            return path
        }
    }
}