package com.mogujie.tt.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.leimo.wanxin.R
import com.mogujie.tt.config.SysConstant
import com.mogujie.tt.imservice.manager.IMLoginManager
import com.mogujie.tt.ui.helper.AudioPlayerHandler
import com.mogujie.tt.ui.helper.AudioRecordHandler
import com.mogujie.tt.utils.CommonUtil
import com.mogujie.tt.utils.Logger
import kotlinx.android.synthetic.main.tt_sound_volume_dialog.view.*
import java.util.*


/**
 * Created by wr
 * Date: 2019/1/16  8:58
 * mail: 1902065822@qq.com
 * describe:
 */
class SoundVolumeView(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {
    private val logger = Logger.getLogger(SoundVolumeView::class.java)
    protected var y1 = 0f
    protected var y2 = 0f
    private var audioRecorderInstance: AudioRecordHandler? = null
    private var audioRecorderThread: Thread? = null
    protected var audioSavePath: String? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.tt_sound_volume_dialog, this)
    }

    fun onPressToSpeakBtnTouch(v: View, event: MotionEvent, recorderCallback: (voiceFilePath: String, voiceTimeLength: Float) -> Unit) {
        if (event.action == MotionEvent.ACTION_DOWN) {

            if (AudioPlayerHandler.getInstance().isPlaying) {
                AudioPlayerHandler.getInstance().stopPlayer()
            }
            y1 = event.y
            v.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_pressed)
//            btnRecordVoice.text = this@MessageBaseActivity.resources.getString(R.string.release_to_send_voice)

            ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_01)
            ivSoundVolume.visibility = View.VISIBLE
            layoutSoundVolume.setBackgroundResource(R.drawable.tt_sound_volume_default_bk)
            visibility = View.VISIBLE
            audioSavePath = CommonUtil.getAudioSavePath(IMLoginManager.instance().loginId)
            // 这个callback很蛋疼，发送消息从MotionEvent.ACTION_UP 判断
            audioRecorderInstance = AudioRecordHandler(audioSavePath, {
                v.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_normal)
                doFinishRecordAudio()
                recorderCallback(audioSavePath ?: "", SysConstant.MAX_SOUND_RECORD_TIME)
            },{sound ->
                onReceiveMaxVolume(sound)
            })
            audioRecorderThread = Thread(audioRecorderInstance)
            audioRecorderInstance?.isRecording = true
            logger.d("message_activity#audio#audio record thread starts")
            audioRecorderThread!!.start()
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            y2 = event.y
            if (y1 - y2 > 180) {
                ivSoundVolume.visibility = View.GONE
                layoutSoundVolume.setBackgroundResource(R.drawable.tt_sound_volume_cancel_bk)
            } else {
                ivSoundVolume.visibility = View.VISIBLE
                layoutSoundVolume.setBackgroundResource(R.drawable.tt_sound_volume_default_bk)
            }
        } else if (event.action == MotionEvent.ACTION_UP) {
            y2 = event.y
            audioRecorderInstance?.isRecording = false
            visibility = View.GONE
            v.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_normal)
//            btnRecordVoice.text = this@MessageBaseActivity.resources.getString(R.string.tip_for_voice_forward)
            if (y1 - y2 <= 180) {
                if (audioRecorderInstance?.recordTime ?: 0f >= 0.5) {
                    if (audioRecorderInstance?.recordTime ?: 0f < SysConstant.MAX_SOUND_RECORD_TIME) {
//                        val msg = uiHandler!!.obtainMessage()
//                        msg.what = HandlerConstant.HANDLER_RECORD_FINISHED
//                        msg.obj = audioRecorderInstance!!.recordTime
//                        uiHandler!!.sendMessage(msg)
                        v.setBackgroundResource(R.drawable.tt_pannel_btn_voiceforward_normal)
                        recorderCallback(audioSavePath ?: "", audioRecorderInstance?.recordTime
                                ?: 0f)
                        doFinishRecordAudio()
                    }
                } else {
                    ivSoundVolume.visibility = View.GONE
                    layoutSoundVolume.setBackgroundResource(R.drawable.tt_sound_volume_short_tip_bk)
                    visibility = View.VISIBLE
                    val timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            try {
                                visibility = View.GONE
                                this.cancel()
                            } catch (e: Exception) {
                            }
                        }
                    }, 700)
                }
            }
        }
    }

    fun doFinishRecordAudio() {
        audioRecorderInstance?.isRecording = false
        visibility = View.GONE
        audioRecorderInstance?.recordTime = SysConstant.MAX_SOUND_RECORD_TIME
    }

    /**
     * @param voiceValue
     * @Description 根据分贝值设置录音时的音量动画
     */
    fun onReceiveMaxVolume(voiceValue: Int) {
        if (voiceValue < 200.0) {
            ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_01)
        } else if (voiceValue > 200.0 && voiceValue < 600) {
            ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_02)
        } else if (voiceValue > 600.0 && voiceValue < 1200) {
            ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_03)
        } else if (voiceValue > 1200.0 && voiceValue < 2400) {
            ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_04)
        } else if (voiceValue > 2400.0 && voiceValue < 10000) {
            ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_05)
        } else if (voiceValue > 10000.0 && voiceValue < 28000.0) {
            ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_06)
        } else if (voiceValue > 28000.0) {
            ivSoundVolume.setImageResource(R.drawable.tt_sound_volume_07)
        }
    }

}