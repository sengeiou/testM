package com.lemo.emojcenter.utils.download

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 */

abstract class FaceDownLoadObserver : Observer<FaceDownloadInfo> {
    protected lateinit var d: Disposable//可以用于取消注册的监听者
    protected lateinit var downloadInfo: FaceDownloadInfo

    override fun onSubscribe(d: Disposable) {
        this.d = d
    }

    override fun onNext(downloadInfo: FaceDownloadInfo) {
        this.downloadInfo = downloadInfo
    }

    override fun onError(e: Throwable) {
        e.printStackTrace()
    }


}
