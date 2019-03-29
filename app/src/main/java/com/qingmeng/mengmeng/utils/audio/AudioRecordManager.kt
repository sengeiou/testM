package com.qingmeng.mengmeng.utils.audio

import android.content.Context
import android.os.Handler
import android.util.Log
import com.czt.mp3recorder.MP3Recorder
import com.qingmeng.mengmeng.utils.ToastUtil
import com.qingmeng.mengmeng.utils.toSelfSetting
import com.qingmeng.mengmeng.view.dialog.DialogCommon
import java.io.File
import java.util.*

/**
 * 录音管理类
 */
class AudioRecordManager(private val mContext: Context) {
    private var onAccentuation: ((voiceValue: Int) -> Unit)? = null     //分贝值返回回调
    private var mAudioPathCallBack: ((path: String, recordTime: Float) -> Unit)? = null     //分贝值返回回调
    private var mMP3Recorder: MP3Recorder? = null                       //音频工具
    private var startTime = 0L                                          //开始时间
    private var recordTime = 0F                                         //音频时间
    private var currentFilePath: String? = null                         //音频路径
    private val MAX_LENGTH = 60                                         //录制最长时间
    var canSendAudio = false                                            //是否可以发送语音（防止个别手机权限提醒不一样）
    private val mHandler = Handler()                                    //子线程查询当前分贝
    private val mUpdateMicStatusTimer = Runnable {
        updateMicStatus()
    }

    /**
     * 准备录音
     */
    fun readyAudio(mDir: String, onAccentuation: (voiceValue: Int) -> Unit = {}, audioPathCallBack: (path: String, recordTime: Float) -> Unit = { _, _ -> }) {
        try {
            this.onAccentuation = onAccentuation
            this.mAudioPathCallBack = audioPathCallBack
            val dir = File(mDir)
            if (!dir.exists()) {
                dir.mkdirs()
            } else {
                if (!dir.isDirectory) {
                    dir.delete()
                    dir.mkdirs()
                }
            }
            val fileName = generateFileName()
            val file = File(dir, fileName)
            currentFilePath = file.absolutePath
            mMP3Recorder = MP3Recorder(file)
            mMP3Recorder?.start()
            startTime = System.currentTimeMillis()
            updateMicStatus()
            canSendAudio = true
        } catch (e: Exception) {
            if (e.toString() == "java.io.IOException: Permission deny!") {
                canSendAudio = false
                //提示用户去设置里设置相应权限
                DialogCommon(mContext, "提示", "使用该功能需要麦克风权限，请前往系统设置开启权限", rightText = "去设置",
                        onRightClick = {
                            toSelfSetting(mContext)
                        }).show()
            }
        }
    }

    //根据分贝改变动画回调
    private fun updateMicStatus() {
        //录音时间超出60秒了
        if (getRecordTime() >= MAX_LENGTH) {
            mMP3Recorder?.stop()
            mMP3Recorder = null
            mHandler.removeCallbacks(mUpdateMicStatusTimer)
            recordTime = getRecordTime()
            mAudioPathCallBack?.invoke(currentFilePath!!, recordTime)
            ToastUtil.showShort("语音最长录制时间为60s,已为您自动发送")
        } else {
            val ratio = mMP3Recorder?.volume ?: 0
            onAccentuation!!(ratio)
            Log.i("ratioTest", "========================================$ratio")
            mHandler.postDelayed(mUpdateMicStatusTimer, 200)
        }
    }

    /**
     * 随机生成文件的名称
     */
    private fun generateFileName(): String {
        return UUID.randomUUID().toString() + ".mp3"
    }

    /**
     * 释放资源
     */
    fun releaseAudio(audioPathCallBack: (path: String, recordTime: Float) -> Unit = { _, _ -> }) {
        if (mMP3Recorder != null) {
            mMP3Recorder?.stop()
            mMP3Recorder = null
            mHandler.removeCallbacks(mUpdateMicStatusTimer)
            recordTime = getRecordTime()
            audioPathCallBack(currentFilePath!!, recordTime)
        }
    }

    fun getRecordTime(): Float {
        return (System.currentTimeMillis() - startTime) / 1000f
    }

    /**
     * 取消录音
     */
    fun cancelAudio() {
        releaseAudio()
        if (currentFilePath != null) {
            val file = File(currentFilePath)
            file.delete()
            currentFilePath = null
        }
    }

    companion object {
        private var mInstance: AudioRecordManager? = null

        fun getInstance(context: Context): AudioRecordManager {
            if (mInstance == null) {
                synchronized(AudioRecordManager::class.java) {
                    if (mInstance == null) {
                        mInstance = AudioRecordManager(context)
                    }
                }
            }
            return mInstance!!
        }
    }
}