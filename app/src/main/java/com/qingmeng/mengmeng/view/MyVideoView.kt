package com.qingmeng.mengmeng.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import cn.jzvd.JZMediaManager
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.qingmeng.mengmeng.MainApplication

class MyVideoView : JzvdStd {
    var onVideoClick = {}
    var outFullscreen = {}

    constructor(context: Context) : super(context)

    constructor(context: Context, att: AttributeSet) : super(context, att)

    override fun playOnThisJzvd() {
        outFullscreen()
    }

    override fun onPrepared() {
        super.onPrepared()
        JZMediaManager.instance().jzMediaInterface.setVolume(0f, 0f)
    }

    override fun onStatePrepared() {

    }

    override fun onClickUiToggle() {
        if (bottomContainer.visibility != View.VISIBLE) {
            setSystemTimeAndBattery()
            clarity.text = jzDataSource.currentKey.toString()
        }
        if (currentState == Jzvd.CURRENT_STATE_PREPARING) {
            changeUiToPreparing()
            if (bottomContainer.visibility == View.VISIBLE) {
            } else {
                setSystemTimeAndBattery()
            }
        } else if (currentState == Jzvd.CURRENT_STATE_PLAYING) {
            JZMediaManager.instance().jzMediaInterface.setVolume(1f, 1f)
            onVideoClick()
        } else if (currentState == Jzvd.CURRENT_STATE_PAUSE) {
            if (bottomContainer.visibility == View.VISIBLE) {
                changeUiToPauseClear()
            } else {
                changeUiToPauseShow()
            }
        }
    }

    override fun onAutoCompletion() {
        MainApplication.secondVideo?.onAutoCompletion()
        super.onAutoCompletion()
    }

    override fun startVideo() {
        super.startVideo()
//        JZMediaManager.instance().jzMediaInterface.setVolume(0f, 0f)
    }
}