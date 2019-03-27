package com.qingmeng.mengmeng.utils.audio

import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.MediaStore
import com.qingmeng.mengmeng.utils.HttpProxyCacheUtil
import com.qingmeng.mengmeng.utils.ToastUtil
import org.jetbrains.anko.async
import java.io.File

/**
 * 播放语音
 */
object MediaManager {
    private var mPlayer: MediaPlayer? = null
    private var mAudioManager: AudioManager? = null
    private var isPause = false        //是否暂停
    var mIsCall = true                 //是否是听筒模式

    //开始播放
    fun play(context: Context, filePath: String, uri: String, playStart: () -> Unit, playFinish: () -> Unit) {
        if (!File(filePath).exists() && uri == "") {
            ToastUtil.showShort("语音地址错误")
            return
        }
        if (File(filePath).exists()) {
            playOk(context, filePath, playStart, playFinish)
        } else {
            val proxy = HttpProxyCacheUtil().getAudioProxy()
            //本地缓存了就直接播放
            if (proxy.isCached(uri)) {
                playOk(context, proxy.getProxyUrl(uri), playStart, playFinish)
            } else {
                if (isNetworkConnected(context)) {
                    playOk(context, proxy.getProxyUrl(uri), playStart, playFinish)
                } else {    //没有网络就直接提示返回
                    ToastUtil.showNetError()
                    return
                }
            }

//            //本地文件存在
//            if (File(getRealFilePath(context, Uri.parse(uri))).exists()) {
//                playOk(context, getRealFilePath(context, Uri.parse(uri)), playStart, playFinish)
//            } else {  //下载文件
//                RequestFileManager.downloadFile(uri, File(IConstants.DIR_AUDIO_STR), object : FileDownLoadObserver<File>() {
//                    override fun onDownLoadSuccess(t: File) {
////                        playOk(context, t.path, playStart, playFinish)
//                        Log.i("yang","onDownLoadSuccess")
//                    }
//
//                    override fun onDownLoadFail(throwable: Throwable) {
//                        ToastUtil.showNetError()
//                        Log.i("yang","onDownLoadFail")
//                    }
//
//                    override fun onComplete() {
//                        super.onComplete()
//                        Log.i("yang","onComplete")
//                    }
//                }) { bytesRead, contentLength, done ->
//
//                }
//            }
        }
    }

    private fun playOk(context: Context, filePath: String, playOk: () -> Unit, playFinish: () -> Unit) {
        if (mPlayer == null) {
            mPlayer = MediaPlayer()
        } else {
            mPlayer?.reset()
        }
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        //默认听筒播放
        mPlayer?.setAudioStreamType(AudioManager.STREAM_VOICE_CALL)
        mPlayer?.setOnErrorListener { mp, what, extra ->
            mPlayer?.reset()
            false
        }
        async {
            mPlayer?.setDataSource(filePath)
            mPlayer?.prepare()
        }
        //MediaPlayer准备完毕了
        mPlayer?.setOnPreparedListener {
            mPlayer?.start()
            playOk()
            mPlayer?.setOnCompletionListener {
                playFinish()
            }
            isPause = false
        }
    }

    //切换播放方式 true听筒 否则扬声器
    fun switchPlay(isCall: Boolean = true) {
        mIsCall = isCall
        mAudioManager?.isSpeakerphoneOn = !isCall
        if (isCall) {
            mAudioManager?.mode = AudioManager.MODE_NORMAL
        }
    }

    //是否正在播放
    fun isPlaying(): Boolean {
        return mPlayer?.isPlaying ?: false
    }

    //暂停播放
    fun pause() {
        if (mPlayer != null && mPlayer?.isPlaying!!) {
            mPlayer?.pause()
            isPause = true
        }
    }

    //继续播放
    fun resume() {
        if (mPlayer != null && isPause) {
            mPlayer?.start()
            isPause = false
        }
    }

    //清除资源
    fun release() {
        if (mPlayer != null) {
            mPlayer?.release()
            mPlayer = null
        }
    }

    //判断有没有网络
    fun isNetworkConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mNetworkInfo = mConnectivityManager.activeNetworkInfo
            if (mNetworkInfo != null) {
                return mNetworkInfo!!.isAvailable
            }
        }
        return false
    }

    //由Uri转成路径的方法
    private fun getRealFilePath(context: Context, uri: Uri): String {
        if (uri == null) return ""
        val scheme = uri.scheme
        var data = ""
        when (scheme) {
            ContentResolver.SCHEME_FILE -> {
                data = uri.path
            }
            ContentResolver.SCHEME_CONTENT -> {
                val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                        if (index > -1) {
                            data = cursor.getString(index)
                        }
                    }
                    cursor.close()
                }
            }
            else -> {
                data = uri.path
            }
        }
        return data
    }
}