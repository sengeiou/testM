package com.lemo.emojcenter.bean

/**
 * Description :
 *
 *
 * Author:fangxu
 *
 *
 * Email:634804858@qq.com
 *
 *
 * Date: 2018/2/5
 */

interface DownloadStatus {
    companion object {
        val download_init = 0
        val download_start = 1
        val download_run = 2
        val download_finish = 3
        val download_error = 4
    }
}
