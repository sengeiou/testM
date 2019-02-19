package com.lemo.emojcenter.utils.download

import java.io.File

/**
 * 下载信息
 */

class FaceDownloadInfo(val url: String) {
    var total: Long = 0
    var progress: Long = 0
    var fileName: String? = null
    var file: File? = null

    companion object {
        val TOTAL_ERROR: Long = -1//获取进度失败
    }
}
