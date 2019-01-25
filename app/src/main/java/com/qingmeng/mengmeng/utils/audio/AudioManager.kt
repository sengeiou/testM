package com.qingmeng.mengmeng.utils.audio

import android.media.MediaRecorder
import android.os.Handler
import android.util.Log
import java.io.File
import java.util.*

/**
 * 录音管理类
 */
class AudioManager(private val mDir: String) {
    private var onAccentuation: ((voiceValue: Int) -> Unit)? = null     //分贝值返回回调
    private var mMediaRecorder: MediaRecorder? = null                   //音频工具
    private var currentFilePath: String? = null                         //音频路径
    private val MAX_LENGTH = 1000 * 60 * 10                             //录制最长时间
    private val mHandler = Handler()                                    //子线程查询当前分贝
    private val mUpdateMicStatusTimer = Runnable {
        updateMicStatus()
    }

    /**
     * 准备录音
     */
    fun readyAudio(onAccentuation: (voiceValue: Int) -> Unit = {}) {
        this.onAccentuation = onAccentuation
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
        mMediaRecorder = MediaRecorder()
        //设置MediaRecorder的音频源为麦克风
        mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        //设置音频格式
        mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        //设置音频编码
        mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        //设置输出文件
        mMediaRecorder?.setOutputFile(file.absolutePath)
        //最长录制时间
        mMediaRecorder?.setMaxDuration(MAX_LENGTH)
        //准备录音
        mMediaRecorder?.prepare()
        //开始
        mMediaRecorder?.start()
        updateMicStatus()
    }

    //根据分贝改变动画回调
    private fun updateMicStatus() {
        val ratio = mMediaRecorder?.maxAmplitude
        onAccentuation!!(ratio!!)
        Log.i("ratioTest", "========================================$ratio")
        mHandler.postDelayed(mUpdateMicStatusTimer, 200)
    }

    /**
     * 随机生成文件的名称
     */
    private fun generateFileName(): String {
        return UUID.randomUUID().toString() + ".amr"
    }

    /**
     * 释放资源
     */
    fun releaseAudio(audioPathCallBack: (path: String) -> Unit = {}) {
        if (mMediaRecorder != null) {
            mMediaRecorder!!.reset()
            mMediaRecorder = null
            mHandler.removeCallbacks(mUpdateMicStatusTimer)
            audioPathCallBack(currentFilePath!!)
        }
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
        private var mInstance: AudioManager? = null

        fun getInstance(dir: String): AudioManager {
            if (mInstance == null) {
                synchronized(AudioManager::class.java) {
                    if (mInstance == null) {
                        mInstance = AudioManager(dir)
                    }
                }
            }
            return mInstance!!
        }
    }
}